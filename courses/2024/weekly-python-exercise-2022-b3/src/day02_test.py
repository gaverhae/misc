# Copyright © Reuven Lerner
# https://lerner.co.il

import pytest
from src.day02 import tr

def test_empty_source():
    with pytest.raises(TypeError):
        noop_tr = tr('', 'abcd')

def test_empty_result():
    with pytest.raises(TypeError):
        noop_tr = tr('abcd', '')

def test_simple():
    a_to_b_tr = tr('a', 'b')
    assert callable(a_to_b_tr)
    assert a_to_b_tr('abcdabcd') == 'bbcdbbcd'

def test_2_to_1():
    ab_to_c_tr = tr('ab', 'c')
    assert ab_to_c_tr('abcdabcd') == 'cccdcccd'

def test_remove():
    ab_to_c_tr = tr('ab', 'x')
    assert ab_to_c_tr('abcdabcd') == 'xxcdxxcd'

def test_example():
    vowels_to_y = tr('aeiou', 'y')
    assert vowels_to_y('the quick brown fox jumps over the lazy dog') == "thy qyyck brywn fyx jymps yvyr thy lyzy dyg"
