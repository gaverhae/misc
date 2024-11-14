# Chapter 1 - Getting Started

This chapter deals primarimy with setting up an environment and installing all
the tools and IDEs.

> I'll be using Vim and installing tools with Nix, so my approach will be a bit
> different from the one recommended in the book.

## Tools

JavaScript is dynamic, which means IDEs don't work as well. Sorry about that.

### What we'll need

The minimal setup we need comprises:

- An IDE.
- A web server.
- A device or device emulator.

> I'll be skipping the emulator, and using Vim as my IDE. The book uses `nginx`
> as the web server, so I'll do that too, but I'll get it from the Nix shell
> rather than installing it as the book suggests.

### Java Development Kit

> Explains how to install Java on Windows 7 and Mac OS X Lion. I'll be using
> Nix.

### Integrated Development Environment

> Explains how to install IntelliJ Idea, WebStorm, and Aptana, and how to
> create a minimal project in each of them.

### Web Server

We need a web server in order to be able to test our site from external devices
(including emulators). We'll use nginx because it is small and, relatively to
Apache, simple.

> I've added `nginx` to the `shell.nix` file, copied the config file over
> (found using `nginx -t`, which prints the path to the config file), made it
> writeable (`chmod +w`), then edited it. Then I created `bin/start-server` to
> serve `public/`. (Also needed to copy `mimes.types` from the same place.)

### The Android SDK and Emulator

> This section explains how to install the emulator and configure virtual
> devices on it. I'm not going to use the emulator at this point (and this
> section is likely the most time-sensitive section of this 10-year-old book),
> but it is a good reminder that I should periodically try the website on
> mobile devices.
>
> Maybe I should think about installing either an iOS or an Android emulator.

## Techniques

### The Code

### Object-Oriented Programming

### A Word About Mobile Browsers
