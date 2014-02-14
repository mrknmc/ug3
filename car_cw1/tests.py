from unittest import TestCase, main
from StringIO import StringIO

from pred import static_predictor, guided_predictor, history_predictor


class TestPredictors(TestCase):

    file_mock = StringIO('\n'.join([
        ('B 402222 1'),
        ('B 402222 1'),
        ('B 402222 1'),
        ('B 402167 1'),
        ('B 402167 0'),
        ('B 402170 1'),
        ('B 402167 0'),
        ('B 402170 1'),
        ('B 402167 1'),
        ('B 402167 0'),
        ('B 402170 1'),
        ('B 402167 0'),
        ('B 402170 1'),
    ]))

    def test_static_predictor_always_taken(self):
        misprediction_rate = static_predictor(self.file_mock, pred=1)
        should_be = 4 / 13.0
        self.assertAlmostEqual(misprediction_rate, should_be)

    def test_static_predictor_always_not_taken(self):
        misprediction_rate = static_predictor(self.file_mock, pred=0)
        should_be = 9 / 13.0
        self.assertAlmostEqual(misprediction_rate, should_be)

    def test_guided_predictor(self):
        """
        Predicted as taken:
            - 402222 - 3/3
            - 402170 - 4/4
            - 402167 - 2/6
        """
        misprediction_rate = guided_predictor(self.file_mock)
        should_be = 2 / 13.0
        self.assertAlmostEqual(misprediction_rate, should_be)

    def test_2bit_history_predictor(self):
        file = StringIO('\n'.join([
            'B 000000 1',
            'B 000000 1',
            'B 000000 1',
            'B 000000 0',
            'B 000000 0',
            'B 000000 0',
        ]))

        misprediction_rate = history_predictor(file, hist_bits=1, state_bits=2, init_state=1)
        should_be = 4 / 6.0
        self.assertAlmostEqual(misprediction_rate, should_be)

        misprediction_rate = history_predictor(file, hist_bits=2, state_bits=2, init_state=1)
        should_be = 5 / 6.0
        self.assertAlmostEqual(misprediction_rate, should_be)

        misprediction_rate = history_predictor(file, hist_bits=3, state_bits=2, init_state=1)
        should_be = 3 / 6.0
        self.assertAlmostEqual(misprediction_rate, should_be)

        misprediction_rate = history_predictor(file, hist_bits=4, state_bits=2, init_state=1)
        should_be = 3 / 6.0
        self.assertAlmostEqual(misprediction_rate, should_be)


if __name__ == '__main__':
    main()
