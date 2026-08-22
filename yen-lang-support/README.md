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

Yen provides a VS Code extension with syntax highlighting and `.yen` file detection.

1. Download `yen-lang-support-0.1.0.vsix` from the Yen release.
2. Open VS Code.
3. Open the **Extensions** view (`Ctrl+Shift+X`).
4. Click the **`...`** menu at the top of the Extensions panel.
5. Select **Install from VSIX...**.
6. Select the downloaded `yen-lang-support-0.1.0.vsix` file.
7. Open a `.yen` file. Yen syntax highlighting should be enabled automatically.
