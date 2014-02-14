CAR Assignment 1 - s1140740
===================
-------------------

I was able to simulate all branch predictors completely.

To run the program on DiCE, run `$ python2.7 pred.py [filename] [predictor_type]` where filename
is the path to the trace file and predictor type is one of the following options:

 - always_taken - Always Taken Predictor that always predicts to take the branch.
 - always_not_taken - Always Not Taken Predictor that always predicts to not take the branch.
 - guided - Profile Guided Predictor that predicts to take the branch only for addresses that have been taken more than 50% time in the profiling stage.
 - fsm - Two-level Adaptive Prediction with initial state 01. You can set the number of bits used for history with the optional argument --hist. By default it is set to 2.

There two are optional arguments:

 - `--hist HIST` - sets the number of bits to use for history to HIST.
 - `--states STATES` - sets the number of bits to use for storing states to STATES.
 - `--help` - explains how to run the program.

You can run `$ python2.7 pred.py -h` to get the same information from the program.

I also wrote unit tests. To run them use `$ python2.7 tests.py`.
