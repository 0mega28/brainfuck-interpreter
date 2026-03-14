sealed interface BFCmd {
    record INC      (int times)     implements BFCmd {}
    record DEC      (int times)     implements BFCmd {}
    record RIGHT    (int times)     implements BFCmd {}
    record LEFT     (int times)     implements BFCmd {}
    record INPUT    ()              implements BFCmd {}
    record OUTPUT   (int times)     implements BFCmd {}
    record JZ       (int addr)      implements BFCmd {}
    record JNZ      (int addr)      implements BFCmd {}
}

class Lexer {
    final static String validCmds = "<>+-.,[]";

    final String src;
    int pos = 0;

    boolean isValidBFCmd(char c) {
        return validCmds.indexOf(c) != -1;
    }

    Lexer(final String src) { this.src = src; }

    char get() {
        while (this.pos < src.length() && !isValidBFCmd(src.charAt(this.pos))) {
            this.pos++;
        }

        if (this.pos >= src.length()) return 0;

        return src.charAt(this.pos);
    }

    void advance() {
        this.pos++;
    }
}

List<BFCmd> parse(final String src) {
    var cmds        = new ArrayList<BFCmd>();
    var lexer       = new Lexer(src);
    char c          = 0;
    var stack       = new ArrayList<Integer>();

    while ((c = lexer.get()) != 0) {
        lexer.advance();
        BFCmd cmd = null;
        switch (c) {
            case '+':
            case '-':
            case '>':
            case '<':
            case '.': {
                int times = 1;
                while (lexer.get() == c) {
                    lexer.advance();
                    times++;
                }

                cmd = switch(c) {
                    case '+' -> new BFCmd.INC(times);
                    case '-' -> new BFCmd.DEC(times);
                    case '>' -> new BFCmd.RIGHT(times);
                    case '<' -> new BFCmd.LEFT(times);
                    case '.' -> new BFCmd.OUTPUT(times);
                    default -> throw new IllegalArgumentException("Illegal command: " + c);
                };
            } break;
            case ',': {
                cmd = new BFCmd.INPUT();
            } break;
            case '[': {
                int addr = cmds.size();
                cmd = new BFCmd.JZ(0);
                stack.add(addr);
            } break;
            case ']': {
                if (stack.isEmpty()) {
                    throw new IllegalStateException("Unbalanced parenthesis: at [" + (lexer.pos - 1) + "]");
                }
                int addr = cmds.size();
                int jmpAddr = stack.removeLast();

                cmd = new BFCmd.JNZ(jmpAddr + 1);
                // backpatch
                assert cmds.get(jmpAddr) instanceof BFCmd.JZ;
                cmds.set(jmpAddr, new BFCmd.JZ(addr + 1));
            } break;
            default:
                throw new IllegalArgumentException("Illegal command: " + c);
        };

        cmds.add(cmd);
    }

    return Collections.unmodifiableList(cmds);
}

void interpret(final List<BFCmd> cmds) {
    int ip          = 0;
    int dp          = 0;
    var DM_SIZE     = 65536;
    var dataMemory  = new int[DM_SIZE];

    while (ip < cmds.size()) {
        var cmd = cmds.get(ip);

        switch (cmd) {
            case BFCmd.INC      (int times): {
                dataMemory[dp] = (dataMemory[dp] + times) % 256;
                ip++;
            } break;
            case BFCmd.DEC      (int times): {
                dataMemory[dp] = ((dataMemory[dp] - times) % 256 + 256) % 256;
                ip++;
            } break;
            case BFCmd.RIGHT    (int times): {
                dp += times;
                assert dp < DM_SIZE;
                ip++;
            } break;
            case BFCmd.LEFT     (int times): {
                dp -= times;
                assert dp >= 0;
                ip++;
            } break;
            case BFCmd.INPUT    ()         : {
                char read = IO.readln().charAt(0);
                dataMemory[dp] = read;
                ip++;
            } break;
            case BFCmd.OUTPUT   (int times): {
                for (int i = 0; i < times; i++) {
                    IO.print((char) dataMemory[dp]);
                }
                ip++;
            } break;
            case BFCmd.JZ       (int addr) : {
                if (dataMemory[dp] == 0) {
                    ip = addr;
                } else {
                    ip++;
                }
            } break;
            case BFCmd.JNZ      (int addr) : {
                if (dataMemory[dp] != 0) {
                    ip = addr;
                } else {
                    ip++;
                }
            } break;
        }
    }
}

void dbgCmd(final List<BFCmd> cmds) {
    for (int i = 0; i < cmds.size(); i++) {
        IO.print(i + ": ");
        IO.println(cmds.get(i));
    }
}

void main(String... args) throws IOException {
    if (args.length != 1) {
        IO.println("Usage: " + "bf" + " <input.bf>");
        System.exit(1);
    }

    final var filePath  = Path.of(args[0]);
    final var src       = Files.readString(filePath);
    final var cmds      = parse(src);

    interpret(cmds);
}
