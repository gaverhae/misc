# gity

## Introduction

For the longest time, I've been doing most of my git visualization with a
wonderful utility called [GitX]. However, that utility has two main problems:

- It only exists for macOS, and I am sometimes working on other platforms, or
  with people who use other platforms.
- It has been unmaintained for over 15 years at this point.

There are a couple forks floating aound the web, but they're a bit hard to
track. I have been using the same binary, which I've transplanted from machine
to machine, for over ten years. It's obviously still an Intel application.

I've been wanting to write my own for a fairly long time now, but it's never
quite been a priority as the existing binary I have still worked. Over the last
week, though, in this very project, the application crashes every time it tries
to render the git history. I'm not sre what's happening or what has changed (it
has worked flawlessly on this repo since this repo was created); it could be a
bug in the app, it could be a bug in Rosetta2.

## A First, Failed Attempt

I've also been looking for an excuse to try [Humble UI], as well as [native
compilation of Clojure programs][humble-graal] through [GraalVM].

So here goes nothing, as they say. We'll see how far I get.

[Humble UI]: https://github.com/HumbleUI/HumbleUI
[GraalVM]: https://www.graalvm.org
[GitX]: https://github.com/pieter/gitx
[humble-graal]: https://github.com/dundalek/humble-graal

Well, I did not actually get very far. Not only could I not get Humble UI to
work with GraalVM, I also could not get GraalVM to work with [Swing] or
[JavaFX], even when removing the "Clojure" variable.

[Swing]: https://docs.oracle.com/javase/tutorial/uiswing/
[JavaFX]: https://openjfx.io

## Explorations

Exploring alternatives, I found a few options:

- **Going Native.** I'm not interested in C++; if I'm going to learn a native
  language in 2025, I want a slightly more modern one. [C] could be an option, as
  could Go or Rust, but ideally I'd lean more towards something like Zig, Odin,
  Mojo, or gren. Unfortunately, it looks like none of them have any GUI library
  of their own, nor mature bindings to the "big three" (Qt, wxWidget, GTK).
  Some of them have "game engine" bindings or libraries, but that means
  reinventing a lot of wheel to get typical GUI controls. It looks like the
  easiest path if I really want to go down that path is to learn C and use GTK
  directly. The [webview-lib] library may offer an alternative route, though
  perhaps if I want to go down that path I may as well go to the next point.
- **Faking It With The Web.** A popular appraoch in recent years, frameworks
  like Electron seem well-suited for the kind of app I have in mind. I have
  briefly surveyed [Electron] (Node.js), [Tauri] (Rust), [Wails] (Go), [NWjs]
  (Node.js), [Sciter] (Node.js), and [Neutralino] (Node.js +). These all have
  roughly similar characteristics in that they can produce "native" packages
  while the GUI part is rendered HTML (either through an embedded [Chromium] or
  using host support), while the backend is coded in the a specific language.
  From my perspective, this should allow me to write all of the GUI parts in
  ClojureScript, which I already know a bit, rather than having to learn an
  entirely new way of doing GUI. [Electron] is the most well-known and looks
  like the most mature one, but it tends to produce fairly bloated packages as
  it bundles Chrome. Backend logic would need to be written in JS, which could
  be ClojureScript, but in an environment I know little about. [Tauri] and
  [Wails] seem like good alternatives, and produce much smaller executable.
  [Rust] seems more interesting to learn than [Go], though I know [Go]'s story
  around cross-compilation and standalone, native executables is pretty
  stellar. [Neutralino] is the only one that mentions first-class support for
  [extensions], i.e. the ability to write your backend in any language. See
  [this comparison][webcomp] for more details.
- **Jank.** [Jank] is meant to be a dialect of Clojure hosted in the C++
  ecossytem rather than the JVM. On paper this seems like the perfect fit, but
  unfortunately the project does not seem quite mature enough yet.
- **Flutter/Dart.** Pushed by Google, the [Flutter] framework uses Google's
  other language [Dart]. I have not heard or read much good about either [Dart]
  or [Flutter], but there is quite a lot of money behind it, so there's a
  chance it mostly works out. There is also a [ClojureDart] dialect that could
  make it easier for me.
- **React Native.** A popular alternative in recent years, [React Native] was
  Facebook's attempt to simplify mobile development, building on the marketing
  momentum and some of the ideas of React. From my understanding, it is mostly
  targeting mobile, but can also be used for desktop apps. Most of the code
  would be written in ClojureScript, accessing native widgets through React
  Native APIs. It's unclear how "portable" the code itself is; I get the
  impression it's a bit of a "portable pogrammer" story. Integration with
  ClojureScript exists, but I'm not sure how good / mature it is. Also, I've
  moved away from React for web development, so I'm note entirely sure how much
  I'd like the same ideas on native.

[C]: https://en.wikipedia.org/wiki/C_(programming_language)
[Go]: https://go.dev
[Rust]: https://www.rust-lang.org
[Zig]: https://ziglang.org
[Odin]: https://odin-lang.org
[Mojo]: https://www.modular.com/mojo
[gren]: https://gren-lang.org
[Qt]: https://www.qt.io/product/framework
[wxWidget]: https://wxwidgets.org
[GTK]: https://www.gtk.org
[Electron]: https://www.electronjs.org
[Tauri]: https://v2.tauri.app
[Wails]: https://wails.io
[NWjs]: https://nwjs.io
[Sciter]: https://sciter.com
[Neutralino]: https://neutralino.js.org
[webview-lib]: https://github.com/webview/webview
[extensions]: https://neutralino.js.org/docs/how-to/extensions-overview
[Chromium]: https://www.chromium.org/Home/
[Jank]: https://jank-lang.org
[Flutter]: https://flutter.dev
[Dart]: https://dart.dev
[ClojureDart]: https://github.com/Tensegritics/ClojureDart
[React Native]: https://reactnative.dev
[webcomp]: https://github.com/Elanis/web-to-desktop-framework-comparison

## Neutralino

The propsect of using a native Clojure executable as an extension to Neutralino
currently takes the lead for me, though Tauri is a close second and Wails is
not far behind.

Well, that didn't work. The documentation was promising at first skim, but
packaging does not seem to actually work on their tutorial application (which
is really just running their template engine), especially on macOS, which is a
bit of a deal breaker. I also get the distinct impression that this is a
one-person project, and the overall level of professionalism is not up to my
standards.

I'm giving up on this; it looks like Electron is a much better option if I
really want to go the web route.

## Web

I spent a fair bit of time looking at their docs, and it seems a bit opaque for
someone who isn't a Rust/Go dev. Which I guess is somewhat expected. They're
still in the running, but I also spent a fair amount of time looking at the
Electron docs, and that actually looks pretty reasonable. I think if I want a
web-style thing I might just admit I need to accept the size of an Electron
bundle.

## `jlink` and `jpackage`

While doing all of that reading, I stumbled on [this HN post][hn] presenting
[this blog post][clj-desktop], which suggests in "modern" Java there is
actually another viable apporoach with a combination of [jpackage] and [jlink].
The core idea here is to "simply" package an JVM application. Unlike GraalVM,
this is not trying to divorce the app from the JVM and produce a native
executable; instead, it is about packaging the JVM with the app. Which
obviously means we keep the drawbacks of the JVM, such as slow startup time and
excessive memory usage, but we do get the one thing I'm actually looking for
here: a downloadable package that "just works" on each major platform, without
requiring users to separately install anything. Maybe. That's my understanding
of the marketing, at least. I'll try it now.

[hn]: https://news.ycombinator.com/item?id=22710604
[clj-desktop]: https://vlaaad.github.io/year-of-clojure-on-the-desktop
[jpackage]: https://docs.oracle.com/en/java/javase/23/docs/specs/man/jpackage.html
[jlink]: https://docs.oracle.com/en/java/javase/23/docs/specs/man/jlink.html
