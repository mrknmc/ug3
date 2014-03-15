# Memory addresses are 48 bits long.
# Data in memory is organized in blocks of 32 bytes.
# Assume a write through cache with LRU replacement policy for set associative caches.
from math import log
from argparse import ArgumentParser
from collections import deque


def set_associative(file_, size=4096, block=32, sets=1):
    """
    Simulates a set-associative cache.
        :param file_: trace file to be used for simulation.
        :param size: size of the cache in bytes
        :param block: size of a block of memory in bytes.
        :param sets: set-associativity.
        :returns: total number of operations, missed number of operations
    """
    cache = {}
    total = {'R': 0, 'W': 0}
    misses = {'R': 0, 'W': 0}

    offset_size = int(log(block, 2))  # e.g. need 5 bits for 32 bytes
    index_size = int(log(size / block / float(sets), 2))

    for op, tag, index, offset in parse(file_, offset_size, index_size):
        total[op] += 1

        if index not in cache:
            # not in cache - add it
            misses[op] += 1
            cache[index] = deque([tag], maxlen=sets)
        else:
            if tag not in cache[index]:
                # append to the end, LRU will be popped
                misses[op] += 1
                cache[index].append(tag)
            else:
                # not very efficient but simple
                cache[index].remove(tag)
                cache[index].append(tag)

    return total, misses


def parse(file_, offset_size, index_size):
    """
    Generator that parses a given file.
        :param file_: file to parse.
        :param offset_size: number of bits used for the offset.
        :param index_size: number of bits used for the index.
        :returns: quadruple of the operation (R|W), tag, index and offset.
    """
    offset_mask = pow(2, offset_size) - 1
    index_mask = pow(2, index_size) - 1

    for line in file_:
        op, addr = line.split()
        addr = int(addr, 16)

        # get offset with a bitmask then shift
        offset = addr & offset_mask
        addr = addr >> offset_size

        # get index with a bitmask then shift
        index = addr & index_mask
        addr = addr >> index_size

        # now only tag left
        tag = addr

        yield op, tag, index, offset

    file_.seek(0)  # reset file pointer


def main(filename=None, cache=None, **kwargs):
    with open(filename) as file_:
        total, misses = set_associative(file_, **kwargs)
        total_sum = sum(total.itervalues())
        misses_sum = sum(misses.itervalues())
        print('Total Miss Rate: {:.4%}'.format(misses_sum / float(total_sum)))
        print('Read Miss Rate: {:.4%}'.format(misses['R'] / float(total['R'])))
        print('Write Miss Rate: {:.4%}'.format(misses['W'] / float(total['W'])))


if __name__ == '__main__':
    parser = ArgumentParser(description='Run a cache simulator on a given trace file.')
    parser.add_argument(
        'filename',
        metavar='tracefile',
        type=str,
        help='Path of the file to process.',
    )
    parser.add_argument(
        '--size',
        type=int,
        default=4096,
        help='Size of the cache in bytes.'
    )
    parser.add_argument(
        '--block',
        type=int,
        default=32,
        help='Size of a block in bytes.'
    )
    parser.add_argument(
        '--sets',
        type=int,
        default=1,
        help='Number of sets in the set-associative cache.'
    )
    args = vars(parser.parse_args())
    main(**args)
