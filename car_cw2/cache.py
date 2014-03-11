# Memory addresses are 48 bits long.
# Data in memory is organized in blocks of 32 bytes.
# Assume a write through cache with LRU replacement policy for set associative caches.
from math import log
from argparse import ArgumentParser
from collections import deque


def parse(file):
    """Parses the file."""
    for line in file:
        op, addr = line.split()
        yield op, int(addr, 16)
    file.seek(0)  # reset file pointer


def set_associative(file, size=4096, block=32, sets=1, **kwargs):
    """"""
    cache = {}
    total = {'R': 0, 'W': 0}
    misses = {'R': 0, 'W': 0}

    offset_size = int(log(block, 2))  # need 5 bits for 32 bytes
    index_size = int(log(size / float(block), 2))

    for op, addr in parse(file):
        tag, index, offset = decode(addr, offset_size, index_size)

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


def direct_mapped(file, size=4096, block=32, **kwargs):
    """Uses a direct-mapped cache with the simulator."""
    cache = {}
    total = {'R': 0, 'W': 0}
    misses = {'R': 0, 'W': 0}

    offset_size = int(log(block, 2))  # need 5 bits for 32 bytes
    index_size = int(log(size / float(block), 2))

    for op, addr in parse(file):
        tag, index, offset = decode(addr, offset_size, index_size)

        total[op] += 1

        if index not in cache:
            # not in cache - add it
            misses[op] += 1
            cache[index] = tag
        else:
            if cache[index] != tag:
                # different tag - replace it
                misses[op] += 1
                cache[index] = tag
            else:
                if op == 'W':
                    # replace the value
                    cache[index] = tag

    return total, misses


def decode(addr, offset_size=8, index_size=7):
    """Decodes a raw address into a block address."""

    # get offset then shift
    offset = addr & int('1' * offset_size, 2)
    addr = addr >> offset_size

    # get index then shift
    index = addr & int('1' * index_size, 2)
    addr = addr >> index_size

    # now only tag left
    tag = addr

    return tag, index, offset


def main(filename=None, cache=None, **kwargs):
    """"""
    caches = {'direct-mapped': direct_mapped, 'set-associative': set_associative}
    with open(filename) as file:
        total, misses = caches[cache](file, **kwargs)
        print 'Total Miss Rate: {:.4%}'.format(sum(misses.values()) / float(sum(total.values())))
        print 'Read Miss Rate: {:.4%}'.format(misses['R'] / float(total['R']))
        print 'Write Miss Rate: {:.4%}'.format(misses['W'] / float(total['W']))


if __name__ == '__main__':
    parser = ArgumentParser(description='Run branch predictors on a given trace file.')
    parser.add_argument(
        'cache',
        metavar='type',
        type=str,
        help='Type of cache: (direct-mapped|set-associative).',
        choices=('direct-mapped', 'set-associative')
    )
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
        default=2,
        help='Number of sets in the set-associative cache.'
    )
    args = vars(parser.parse_args())
    main(**args)
