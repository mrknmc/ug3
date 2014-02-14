from argparse import ArgumentParser
from collections import defaultdict


def history_predictor(file, hist_bits=2, state_bits=2, init_state=0):
    """
    Predictor using multiple FSMs with a certain history size for each address.
        :param file: trace file used for prediction
        :param hist_bits: number of bits used for history
        :param state_bits: number of bits used for states in FSM
        :param init_state: initial state of the FSM
        :returns: misprediction rate
    """
    total, correct = 0.0, 0
    states = pow(2, state_bits) - 1  # number of states per FSM
    half_state = states / 2.0

    # initialise predictors
    preds = defaultdict(lambda: defaultdict(lambda: init_state))
    # history starts with all not taken
    hists = defaultdict(lambda: (0,) * hist_bits)

    for addr, taken in parse(file):
        total += 1
        pred = preds[addr]  # dict of predictors for this address
        hist = hists[addr]  # history for this address
        state = pred[hist]  # state of the predictor for current history
        should_take = 1 if state > half_state else 0
        correct += should_take == taken
        # increment or decrement the state based on the prediction
        pred[hist] = min(state + 1, states) if taken else max(state - 1, 0)
        hists[addr] = hist[1:] + (taken, )  # add last info to history

    return 1 - (correct / total)


def guided_predictor(file):
    """
    Predictor where a branch is taken only if the address has been taken more
    than 50% of the time in the profiling file.
        :param file: trace file used for prediction
        :returns: misprediction rate
    """
    total, correct = 0.0, 0
    addr_taken = defaultdict(lambda: (0, 0))
    for addr, taken in parse(file):
        num_taken, num_found = addr_taken[addr]
        addr_taken[addr] = num_taken + taken, num_found + 1

    for addr, taken in parse(file):
        total += 1
        took, addr_total = addr_taken[addr]
        should_take = 1 if took > 0.5 * addr_total else 0
        correct += taken == should_take
    return 1 - (correct / total)


def static_predictor(file, pred):
    """
    Predictor where a branch is either always taken or not taken.
        :param file: trace file used for prediction
        :param pred: value we will always predict
        :returns: misprediction rate
    """
    total, correct = 0.0, 0
    for addr, taken in parse(file):
        total += 1
        correct += taken == pred
    return 1 - (correct / total)


def parse(file):
    """
    Generator that parses a given file.
        :param file: file to parse
        :returns: a tuple of an address and whether the branch was taken
    """
    for line in file:
        b, addr, taken = line.split()
        yield addr, int(taken)
    file.seek(0)  # reset file pointer


def main(filename=None, pred_name=None, hist=2, states=2):
    preds = {
        'always_taken': ('Always Taken Predictor', lambda: static_predictor(file, 1)),
        'always_not_taken': ('Always Not Taken Predictor', lambda: static_predictor(file, 0)),
        'guided': ('Profile Guided Predictor', lambda: guided_predictor(file)),
        'fsm': (
            'FSM Predictor with {}-bit History'.format(hist),
            lambda: history_predictor(file, hist_bits=hist, state_bits=states, init_state=1)
        )
    }

    with open(filename) as file:
        name, pred = preds[pred_name]
        print('Misprediction Rate for {}:'.format(name))
        print('{:.4%}'.format(pred()))


if __name__ == '__main__':
    parser = ArgumentParser(description='Run branch predictors on a given trace file.')
    parser.add_argument(
        'filename',
        metavar='tracefile',
        type=str,
        help='Path of the file to process.',
    )
    parser.add_argument(
        'pred_name',
        metavar='predictor',
        type=str,
        help='Which predictor to use: (always_taken|always_not_taken|guided|fsm)',
        choices=('always_taken', 'always_not_taken', 'guided', 'fsm')
    )
    parser.add_argument(
        '--hist',
        type=int,
        default=2,
        help='Number of bits to use for the history.'
    )
    parser.add_argument(
        '--states',
        type=int,
        default=2,
        help='Number of bits to use for the history.'
    )
    args = vars(parser.parse_args())
    main(**args)
