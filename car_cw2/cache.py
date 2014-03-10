# Memory addresses are 48 bits long.
# Data in memory is organized in blocks of 32 bytes.
# Assume a write through cache with LRU replacement policy for set associative caches.
from sys import argv
from math import log


def parse(file):
    """Parses the file."""
    for line in file:
        op, addr = line.split()
        yield op, int(addr, 16)
    file.seek(0)  # reset file pointer


def set_associative(file, ):
    """"""
    pass


def direct_mapped(file, cache_size=4096, block_size=32):
    """Uses a direct-mapped cache with the simulator."""
    cache = {}
    misses = {'R': 0, 'W': 0}

    offset_size = int(log(block_size, 2))  # need 5 bits for 32 bytes
    index_size = int(log(cache_size / float(block_size), 2))

    for op, addr in parse(file):
        tag, index, offset = decode(addr, offset_size, index_size)

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

    return misses


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


def main(filename):
    """"""
    with open(filename) as file:
        print direct_mapped(file)


if __name__ == '__main__':
    # parser = ArgumentParser(description='Run branch predictors on a given trace file.')
    main(filename=argv[1])
