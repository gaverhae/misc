let
  pkgs = import ./nix/nixpkgs.nix;
  jdk = pkgs.openjdk21_headless;
in
pkgs.mkShell {
  LOCALE_ARCHIVE = if pkgs.stdenv.isLinux then "${pkgs.glibcLocales}/lib/locale/locale-archive" else "";
  buildInputs = with pkgs; [
    babashka
    bash
    curl
    jdk
    jq
    (leiningen.override { jdk = jdk; })
  ];
}
