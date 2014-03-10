from unittest import TestCase, main
from cache import decode


class TestDecode(TestCase):

    def setUp(self):
        pass

    def test_decode(self):
        """
              tag            index     offset
        10001100110110100 | 0111111 | 10101000
        """

        addr = int('8cda3fa8', 16)

        right_tag = int('10001100110110100', 2)
        right_index = int('0111111', 2)
        right_offset = int('10101000', 2)

        tag, index, offset = decode(addr, offset_size=8, index_size=7)
        self.assertEqual(tag, right_tag)
        self.assertEqual(offset, right_offset)
        self.assertEqual(index, right_index)

if __name__ == '__main__':
    main()
