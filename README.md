# Example of populating [KeyStore]s from on disk SSL Certificates

Working with SSL certificates on the JVM can be something of a hassle
and is certainly unintuitive. This repository serves as a reference for
how to use (base64 PEM encoded) SSL certificates with a Ring web server.

The mechanics of creating and populating a [KeyStore] are abstracted
away such that all that needs to be provided are the file paths to the
certificate and private key.

## Usage

Create a configuration file, in EDN format, containing:

* `:http-port` the port to bind the non-SSL listener to.
* `:ssl-port` the port to bind the SSL listener to.
* `:private-key` either the path to the private key file or the private
  key itself, as a string.
* `:certificate-chain` a vector of certificate paths/contents; the first
  element should be the server certificate with the remainder being any
  intermediate Certificate Authority certificates that should also be
  provided to clients.

Then, simply:

    lein run -c path/to/config.edn

… but the point isn’t really to _run_ it so much as it is to review and
take something away from the code.

[keystore.clj] contains everything related to turning base64 PEM encoded
certificates and private keys into a [KeyStore] suitable for use with
Ring (or virtually anything else that utilises SSL on the JVM.)

The `enable-ssl` function in [core.clj] shows how the keystore methods
are used prior to instantiating a Jetty instance.

That’s pretty much all there is to it. [config.clj] and [pem.clj] do
a lot of heavy lifting to make sure that we’re resonably confident that
the specified configuration does, in fact, contain a valid
certificate/private key pair.

## TODO

* Command line configuration overrides?
* Integration with [Vault]? (That’s mainly just an exercise in hooking
  up Vault as a source of the configuration data, it’ll just give us PEM
  strings that we can feed into the server functions as-is.)

## License

Copyright © 2017 Adam Harper

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[KeyStore]: https://docs.oracle.com/javase/7/docs/api/index.html?java/security/KeyStore.html
[Vault]: https://vaultproject.io/
[keystore.clj]: src/sslt/keystore.clj
[core.clj]: src/sslt/core.clj
[config.clj]: src/sslt/config.clj
[pem.clj]: src/sslt/util/pem.clj
