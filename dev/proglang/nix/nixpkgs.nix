let
  spec = builtins.fromJSON (builtins.readFile ./src.json);
in
  import (builtins.fetchTarball {
    url = "https://github.com/${spec.owner}/${spec.repo}/archive/${spec.commit}.tar.gz";
    sha256 = spec.sha;
  })
