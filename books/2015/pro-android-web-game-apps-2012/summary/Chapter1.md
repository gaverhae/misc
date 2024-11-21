[Previous](./Introduction.md) | [Next](./Chapter2.md)

<hr>

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

> You can check the setup works for you by opening the project folder, running
> `direnv allow`, then running `server start`. This will start an nginx web
> server with a simple HTML page at [/ch1/](http://127.0.0.1:8080/ch1/).
>
> If you want to follow along, I recommend reading the rest of this summary
> with that server running.
>
> Note that the server listens to `0.0.0.0` and prints a best-effort guess of
> its local IP address on startup, so if you have other devices on the same
> local network you may be able to use that address to connect to it. YMMV.

### The Android SDK and Emulator

> This section explains how to install the emulator and configure virtual
> devices on it. I'm not going to use the emulator at this point (and this
> section is likely the most time-sensitive section of this 10-year-old book),
> but it is a good reminder that I should periodically try the website on
> mobile devices.
>
> Maybe I should think about installing either an iOS or an Android emulator.

## Techniques

Write code you can be proud of.

### The Code

Use `_` to denote a private member of an object; name constructor functions
with a capital letter; don't try to write cleverly short code; don't make (too
many) structural changes; don't change variable types; use asynchronous code
where possible; notify the user about long operations (>1s); handle missing
resources even when they should be there.

In short: clear, predictable, and fault-tolerant.

### Object-Oriented Programming

JavaScript (_in 2012_) does not have classes, but we can emulate them using the
prototype mechanism. In short:

- Every object has a _prototype_, which can be accessed (but generally
  shouldn't be) with `Object.getPrototypeOf(obj)`.
- When a method is called on an object (`obj.method(*args)`, or the more
  general `method.call(obj, *args)`), the special value `this` in the method
  body refers to that object.
- When a property of an object is accessed (`obj.prop`), the JavaScript runtime
  will check for the property on the object first, then on its prototype,
  recursively until we reach the "root" prototype `Object`.
- Literal objects (`{...}`) have `Object` as their prototype.
- To create an object with a different prototype, use `Object.create(proto,
  props)` rather than literal object syntax.
- Every function object (or "function" for short) has a `prototype` property
  (which is _not_ the prototype of the function object itself).
- The notation `obj = new Func()` is conceptually equivalent to:
  ```javascript
  obj = Object.create(Func.prototype);
  Func.call(obj);
  ```

Putting this all together, we can devise a simple class-like system with:

```javascript
function extend(sub, sup) {
  sub.prototype = Object.create(sup.prototype, {
    constructor: {
      value: sub,
      enumerable: false,
      writable: true,
      configurable: true
    }
  });
}

// Creating a constructor for a class:
function Car(color) {
  this._color = color;
}

// Defining "object methods" for members of that "class":
Car.prototype.move = function () {...}
Car.prototype.turn = function () {...}

// Creating a subclass:
function FireTruck() {
  // explicit call to parent constructor
  Car.call(this, "red");
}

// Required explicit inheritance chain definition
extend(FireTruck, Car);

// Subclass-specific methods:
FireTruck.prototype.turnCannon = function () {...}
```

Note that changes to `Function.prototype` are reflected in the behaviour of all
objects created from `Function`, even ones created _before_ those changes
happen (as they still have a reference to the same `Function.prototype`
object).

### A Word About Mobile Browsers

They suck, and there are a lot of them.

> This has gotten a bit better now.

<hr>

[Previous](./Introduction.md) | [Next](./Chapter2.md)
