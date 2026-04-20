# Extensible Hash Table

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Graphviz](https://img.shields.io/badge/Graphviz-FF69B4?style=for-the-badge)

## Description

This project consists of the implementation of a **Dynamic Extensible Hash Table**, developed as part of the _Algorithms and Programming: Advanced Data Structures_ class, from the Computer Science course at **Universidade do Vale do Rio dos Sinos**.

The application aims to support fundamental operations such as **insertion**, **search**, and **deletion**, while dynamically managing its internal structure using techniques like **bucket splitting and merging**.

## Contributors

- Carlos Eduardo Polidori Wendling
- Bernardo Dauber Vieira

## Tech Stack

- **Java** – Core implementation language
- **Graphviz** – Used for visualizing the hash table structure

## Requirements

To run this project locally, make sure you have installed:

- **[Java JDK](https://www.oracle.com/br/java/technologies/downloads/#jdk26-windows) (version 8 or higher)**
- **[Graphviz](https://graphviz.org/download/)**

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/CarlosWendling/extendable-hash.git
cd hash-extensivel
```

### 2. Compile the Project

```bash
javac hash/extendable/*.java
```

### 3. Run the Application

```bash
java hash.extendable.Main
```

### 4. View Generated Visualizations

After running the application, DOT and SVG files will be generated in the `dotFiles/` and `svgFiles/` directories, respectively. Each test produces a visualization of the hash table structure at key points:

- `test1_basic.dot/svg`: After basic insertions
- `test2_update.dot/svg`: After value update
- `test3_split.dot/svg`: After bucket split
- `test4_remove_before.dot/svg`: Before removals
- `test4_remove_after.dot/svg`: After removals and merges
- `test5_full_after_inserts.dot/svg`: After full insertions
- `test5_full_after_removes.dot/svg`: After partial removals

Open the SVG files in any web browser or image viewer to see the visual representation of the extensible hash table.
