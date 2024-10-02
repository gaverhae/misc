let
  pkgs = import ./nix/nixpkgs.nix;
  jdk = pkgs.openjdk17_headless;
in
pkgs.mkShell {
  LOCALE_ARCHIVE = if pkgs.stdenv.isLinux then "${pkgs.glibcLocales}/lib/locale/locale-archive" else "";
  buildInputs = with pkgs; [
    bash
    curl
    jq
    (leiningen.override { jdk = jdk; })
  ];
}
