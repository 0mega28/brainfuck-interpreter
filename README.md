# Brainfuck Interpreter in Java

A minimal **Brainfuck interpreter written in Java**, implemented using modern Java features such as **sealed interfaces, records, and pattern matching in switch**.

The project includes:

* A **lexer** that filters valid Brainfuck commands
* A **parser** that converts source code into optimized instructions
* An **interpreter** that executes the instructions on a Brainfuck memory model

---

# Features

* Implemented using **Java sealed interfaces and records**
* **Instruction compression**
  Consecutive operations like `++++` or `>>>>` are compressed into a single instruction with a `times` count
* **Jump backpatching** for `[` and `]`
* **Fixed memory tape** of `65536` cells
* **Byte wrapping (0–255)** as per Brainfuck semantics
* **Assertion-based safety checks** for pointer bounds

---

# Brainfuck Language Overview

Brainfuck is a minimalistic programming language with only **8 commands**:

| Command | Description                   |
| ------- | ----------------------------- |
| `>`     | Move data pointer right       |
| `<`     | Move data pointer left        |
| `+`     | Increment cell value          |
| `-`     | Decrement cell value          |
| `.`     | Output character              |
| `,`     | Input character               |
| `[`     | Jump forward if cell is zero  |
| `]`     | Jump back if cell is non-zero |

All other characters are ignored.

---

# Architecture

The interpreter is divided into three stages.

## 1. Lexer

The lexer scans the source and filters valid Brainfuck commands.

```java
final static String validCmds = "<>+-.,[]";
```

Any non-command characters are ignored, allowing comments or whitespace in the source file.

---

## 2. Parser

The parser converts the Brainfuck program into a list of **typed commands**.

```java
sealed interface BFCmd {
    record INC(int times) implements BFCmd {}
    record DEC(int times) implements BFCmd {}
    record RIGHT(int times) implements BFCmd {}
    record LEFT(int times) implements BFCmd {}
    record INPUT() implements BFCmd {}
    record OUTPUT(int times) implements BFCmd {}
    record JZ(int addr) implements BFCmd {}
    record JNZ(int addr) implements BFCmd {}
}
```

### Optimization

The parser **compresses repeated instructions**:

```
+++++   -> INC(5)
>>>>    -> RIGHT(4)
....    -> OUTPUT(4)
```

### Loop Handling

Loops are implemented with **jump instructions** using a stack and **backpatching**:

```
[ ... ]
```

becomes

```
JZ  -> jump forward if cell == 0
JNZ -> jump back if cell != 0
```

---

## 3. Interpreter

The interpreter executes the parsed commands using:

* **Instruction Pointer (IP)**
* **Data Pointer (DP)**
* **Data Memory**

```java
var dataMemory = new int[65536];
```

Memory values wrap within **0–255**, matching Brainfuck byte behavior.

---

# Running the Interpreter

Compile and run with assertions enabled:

```bash
java -ea BrainFuck.java hello.bf
```

---

# Debugging

You can inspect the parsed instructions using:

```java
dbgCmd(cmds);
```

This prints the internal instruction representation with addresses.

---

# Possible Improvements

Some interesting extensions:

* Brainfuck **JIT compilation**
* **Dynamic tape growth**
* **Better IO handling**
* **Debug mode / step execution**

---

# Learning Goals

This project explores:

* Interpreter design
* Parsing and bytecode-like instruction models
* Jump backpatching
* Modern Java language features
* Low-level execution models
