import tkinter as tk
from tkinter import filedialog
from py4j.java_gateway import JavaGateway
from py4j.java_gateway import GatewayParameters

gateway = JavaGateway(gateway_parameters = GatewayParameters(port=25530))

def save_text():
    text_content = text_input.get("1.0", tk.END)
    file_path = filedialog.asksaveasfilename(defaultextension=".txt", filetypes=[("Text files", "*.txt")])
    if file_path:
        with open(file_path, "w") as file:
            file.write(text_content)

def open_text():
    file_path = filedialog.askopenfilename(filetypes=[("Text files", "*.txt")])
    if file_path:
        with open(file_path, "r") as file:
            text_input.delete("1.0", tk.END)
            text_input.insert("1.0", file.read())

def run_code():
    text_output.delete("1.0", tk.END)  # Clear previous content
    input_text = text_input.get("1.0", tk.END)

    gateway.entry_point.set_text(input_text)
    res = gateway.entry_point.get_result()
    res = res.replace(",",'\n')
    text_output.insert("1.0", res)
    # if error:
    #     text_output.insert("1.0", error.as_string())
    # elif result:
    #     if len(result.elements) == 1:
    #         text_output.insert("1.0", repr(result.elements[0]))
    #     else:
    #         text_output.insert("1.0", repr(result))

# Create the main window
root = tk.Tk()
root.title("Flex Code Editor")

# Configure background color for the root window
root.configure(bg="black")

# Create a frame for the buttons and place it at the top-center
button_frame = tk.Frame(root, bg="black")
button_frame.pack(side=tk.RIGHT, fill=tk.X)

# Create buttons and add them to the button frame
button_run = tk.Button(button_frame, text="Run", command=run_code, bg="gray", fg="black")
button_run.pack(side=tk.TOP, padx=5, pady=5)

button_save = tk.Button(button_frame, text="Save", command=save_text, bg="gray", fg="black")
button_save.pack(side=tk.TOP, padx=5, pady=5)

button_open = tk.Button(button_frame, text="Open", command=open_text, bg="gray", fg="black")
button_open.pack(side=tk.TOP, padx=5, pady=5)

# Create a frame to hold the text input and output widgets
text_frame = tk.Frame(root, bg="black")
text_frame.pack(expand=True, fill=tk.BOTH)

# Text widget for input
text_input = tk.Text(text_frame, wrap=tk.WORD, bg="black", fg="white")
text_input.pack(side=tk.LEFT, expand=True, fill=tk.BOTH, padx=5, pady=5)

# Text widget for output
text_output = tk.Text(text_frame, wrap=tk.WORD, bg="black", fg="white")
text_output.pack(side=tk.RIGHT, expand=True, fill=tk.BOTH, padx=5, pady=5)

# Run the main event loop
root.mainloop()
