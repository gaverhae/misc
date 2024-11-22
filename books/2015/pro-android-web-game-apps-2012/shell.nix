let
  pkgs = import ./nix/nixpkgs.nix;
  getFlake = url: (builtins.getFlake url).packages.${pkgs.system}.default;
  jdk = pkgs.openjdk17_headless;
in
pkgs.mkShell {
  SSL_CERT_FILE = "${pkgs.cacert}/etc/ssl/certs/ca-bundle.crt";
  LOCALE_ARCHIVE = if pkgs.stdenv.isLinux then "${pkgs.glibcLocales}/lib/locale/locale-archive" else "";
  buildInputs = with pkgs; [
    bash
    curl
    jdk
    jq
    nettools
    nginx
    nodejs
  ];
}
