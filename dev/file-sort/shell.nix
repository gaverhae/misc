let
  pkgs = import ./nix/nixpkgs.nix;
  getFlake = url: (builtins.getFlake url).packages.${pkgs.system}.default;
  jdk = pkgs.openjdk21_headless;
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
