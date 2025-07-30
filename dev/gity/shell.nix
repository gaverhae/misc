let
  pkgs = import ./nix/nixpkgs.nix {};
  jdk = pkgs.jdk21;
  getFlake = url: (builtins.getFlake url).packages.${pkgs.system}.default;
in
pkgs.mkShell {
  LOCALE_ARCHIVE = if pkgs.stdenv.isLinux then "${pkgs.glibcLocales}/lib/locale/locale-archive" else "";
  buildInputs = with pkgs; [
    bash
    cacert
    (clojure.override { jdk = jdk; })
    curl
    graalvmPackages.graalvm-ce
    jdk
    jq
  ];
}
