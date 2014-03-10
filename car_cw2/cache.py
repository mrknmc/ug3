# Memory addresses are 48 bits long.
# Data in memory is organized in blocks of 32 bytes.
# Assume a write through cache with LRU replacement policy for set associative caches.


def parse(file):
    """Parses the file."""
    for line in file:
        op, addr = line.split()
        yield op, int(addr, 16)
    file.seek(0)  # reset file pointer


def direct(file, cache_size=4096, block_size=32, sets=1):
    """Uses a direct-mapped cache with the simulator."""
    cache = {}
    cur_size = 0
    for op, addr in parse(file):
        addr = decode(addr)


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


def main(filename=None):

    with open(filename) as file:
        pass
