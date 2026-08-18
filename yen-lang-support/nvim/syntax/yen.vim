if exists("b:current_syntax")
  finish
endif

" Keywords
syn keyword yenConditional if else
syn keyword yenRepeat for while
syn keyword yenStatement return print
syn keyword yenLogicalOp and or
syn keyword yenBoolean true false

" Types
syn keyword yenType void int double bool string

" Comments — Yen only supports // comments
syn match yenComment "//.*$" contains=@Spell

" Strings
syn region yenString start=/"/ skip=/\\"/ end=/"/ contains=yenEscape
syn match yenEscape contained "\\."

" Numbers — define floats before integers
syn match yenFloat "\<\d\+\.\d\+\>"
syn match yenNumber "\<\d\+\>"

" Function names — identifier immediately followed by '('
syn match yenFunction "\<[A-Za-z_][A-Za-z0-9_]*\>\ze\s*("

" Operators
syn match yenOperator "==\|!=\|<=\|>=\|=\|<\|>\|!\|+\|-\|\*\|/"

" Punctuation
syn match yenDelimiter "[{}();,.]"

let b:current_syntax = "yen"

hi def link yenConditional Conditional
hi def link yenRepeat      Repeat
hi def link yenStatement   Statement
hi def link yenLogicalOp   Keyword
hi def link yenBoolean     Boolean
hi def link yenType        Type
hi def link yenComment     Comment
hi def link yenString      String
hi def link yenEscape      SpecialChar
hi def link yenNumber      Number
hi def link yenFloat       Float
hi def link yenFunction    Function
hi def link yenOperator    Operator
hi def link yenDelimiter   Delimiter
