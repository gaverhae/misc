import pathlib
import tarfile
import tempfile
import zipfile

def tar_to_zip(*files, **kwargs):
    out = kwargs.get('zippath', '.')
    print(out)
    for file in files:
        if tarfile.is_tarfile(file):
            with tempfile.TemporaryDirectory() as extract_dir:
                with tarfile.open(file) as tf:
                    tf.extractall(extract_dir)
                with zipfile.ZipFile(out / file.with_suffix(".zip"), 'x') as zf:
                    for extracted_file in pathlib.Path(extract_dir).glob('*'):
                        zf.write(extract_dir / extracted_file)
        else:
            print("Couldn't read from: " + str(file))

