# Yen

A statically-typed, bytecode-compiled scripting programming language that runs on a custom VM, implemented in Java.

Full walkthrough: [docs/yenDocument.pdf](docs/yenDocument.pdf)

## Installation

Download the appropriate executable from [Yen releases](https://github.com/ithropic/Yen-Compiler/releases/tag/v0.1.0). No Java installation required - these are native executables.

**Linux x86_64:**
Download yen-linux-x86_64 and install.sh from the Yen [releases/](https://github.com/ithropic/Yen-Compiler/tree/main/releases).

Place both files on the same directory and run:

```bash
chmod +x install.sh 
./install.sh 
```

The installer place the Yen executable at:

~/.local/bin/yen

You can then run Yen from anywhere:

```bash
yen program.yen
```

**Windows x86_64:**

Download:

yen-windows-x86_64.exe

Then run it from PoweShell or Command Prompt:

```powershell
.\yen-windows-x86_64.exe program.yen
```

**macOS Apple Silicon:**

Download:

yen-macos-aarch64

Then make it executable:

```bash
chmod +x yen-macos-aarch64

#place it somewhere in your PATH:

sudo cp yen-macos-aarch64 /usr/local/bin/yen
```

You can then run:

```bash
yen program.yen
```

<video src="https://github.com/user-attachments/assets/d611a893-512c-45ad-9e0c-d9ec6ef18348" width="100%"></video>

## Usage

Yen programs use the .yen file extension.

For example:

```bash
cat yen_features.yen
```

```C

// comment

bool isEven(int n) {
    if (n == 0) {
        return true;
    } else {
        return isEven(n - 1) == false;
    }
}

double average(double a, double b) {
    return (a + b) / 2;
}


    int count = 0;
    while (count < 5) {
        print count;
        count = count + 1;
    }

    for (int i = 0; i < 3; i = i + 1) {
        if (isEven(i) and i > 0) {
            print "even and positive";
        } else if (isEven(i) or i == 0) {
            print "even or zero";
        } else {
            print "odd";
        }
    }

    string label = "result";
    double avg = average(2.5, 7.5);
    bool valid = (avg > 0 and avg != 100);

    if (!valid) {
        print "invalid";
    }

    print label;
```

Output:

```
0
1
2
3
4
even or zero
odd
even and positive
result
```

This covers every type (`int`, `double`, `bool`, `string`, `void`), both loop
forms, `if`/`else if`/`else`, function declarations with explicit return
types, recursion, `and`/`or`/`!`, comparison and arithmetic operators, and
`print`.

## Editor support

Syntax highlighting for `.yen` files is available for VS Code and Neovim.
See [`yen-lang-support/README.md`](./yen-lang-support/README.md) for install
steps, or grab the packaged VS Code extension from the
[release assets](https://github.com/ithropic/Yen-Compiler/releases/latest)
(`yen-lang-support-0.1.0.vsix`).

## Building from source

Requires Java 21+ and Maven.

```bash
git clone https://github.com/ithropic/Yen-Compiler.git
cd Yen-Compiler
mvn clean package
java -jar target/yen-0.1.0.jar hello.yen
```

To build a native executable yourself (requires
[GraalVM for JDK 21](https://www.graalvm.org/downloads/) and its
Native Image component):

```bash
native-image -jar target/yen-0.1.0.jar
```

## License

MIT — see [LICENSE](./LICENSE).
