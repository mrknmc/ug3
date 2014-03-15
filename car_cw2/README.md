CAR Assignment 2 - s1140740
===================

I was able to do the cache simulator completely.

To run the program on DiCE, run:

    $ python2.7 cache.py [--size SIZE] [--block BLOCK] [--sets SETS] tracefile

where `tracefile` is the path to the trace file.

There are three optional arguments:

 - `--size SIZE` - size of the cache in bytes.
 - `--block BLOCK` - size of a cache block in bytes.
 - `--sets SETS` - set-associativity of the cache (set to 1 for direct-mapped).

You can also run:

    $ python2.7 cache.py -h

to get the same information from the program.
