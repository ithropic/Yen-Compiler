# Yen Language Support

Syntax highlighting and `.yen` file detection for the Yen programming language.

## Neovim / LazyVim

The `nvim/` directory can be used as a small local plugin:

```lua
{
  dir = "~/path/to/yen-lang-support/nvim",
  name = "yen-lang-support",
  ft = "yen",
}
```

Or copy it directly into your Neovim config:

```bash
cp -r nvim/* ~/.config/nvim/
```

Yen uses **only `//` line comments**. There is deliberately no `/* ... */` comment support.

## VS Code

The `vscode/` directory is a complete local VS Code extension:

```bash
cp -r vscode ~/.vscode/extensions/yen-lang-support-0.1.0
```

Then reload VS Code.

The extension recognizes `.yen` files automatically and provides TextMate syntax highlighting.
