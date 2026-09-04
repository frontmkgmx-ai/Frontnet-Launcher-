with open('app/src/main/java/com/example/ui/components/CustomizeLauncherSheet.kt') as f:
    text = f.read()

def check_braces(text):
    stack = []
    for i, c in enumerate(text):
        if c == '{':
            stack.append(i)
        elif c == '}':
            if not stack:
                print("Extra } at index", i)
                return
            stack.pop()
    if stack:
        print("Unclosed { at index", stack)
    else:
        print("Braces are balanced")

check_braces(text)
