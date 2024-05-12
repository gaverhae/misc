# Copyright © Reuven Lerner
# https://lerner.co.il

import pytest
import tarfile
import zipfile

from src.day03 import tar_to_zip
from io import StringIO


@pytest.fixture
def test_tarfile(tmp_path):

    for index, letter in enumerate('abcde', 1):
        with open(tmp_path / f'{letter * index}.txt', 'w') as f:
            f.write(f'{letter * index}\n' * 100)

    tf = tmp_path / 'mytar.tar'
    with tarfile.open(tf, 'w') as t:
        for index, letter in enumerate('abcde', 1):
            t.add(tmp_path / f'{letter * index}.txt')

    return tf


@pytest.fixture
def test_textfile(tmp_path):
    tf = tmp_path / 'testfile.txt'
    with open(tf, 'w') as f:
        for i in range(10):
            f.write(f'Line {i}\n')

    return tf

def test_tar_to_zip(tmp_path, test_tarfile):
    tar_to_zip(test_tarfile, zippath=tmp_path)

    assert len(list(tmp_path.glob('*.zip'))) == 1

    zf = zipfile.ZipFile(tmp_path / 'mytar.zip')
    zf.extractall(path=tmp_path)
    assert len(list(tmp_path.glob('*.txt'))) == 5

def test_bad_file(tmp_path, test_textfile, capsys):
    tar_to_zip(test_textfile)

    captured_stdout, captured_stderr = capsys.readouterr()

    assert "Couldn't read from" in captured_stdout
