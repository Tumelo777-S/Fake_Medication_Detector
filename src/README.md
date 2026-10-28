# Fake Medicine Detection in South Africa

Group: Cornerstone Coders
Course: Computer Science 3A Mini Project
Members: O Ntebe, N Dube, MJ Makhavhu, TM Mphahlele

---

## What This Project Is About

Fake medicine is a serious health problem in South Africa and it puts a lot of lives at risk every year. We built a Java desktop application that can detect counterfeit medicines by looking at photos of their packaging and pills.

The way it works is that each image gets converted into a graph. The graph has nodes that represent small regions of the image and edges that connect regions that look similar to each other. From there the system runs two things. First it does similarity detection where it compares the uploaded image to a database of known medicines. Second it classifies the image as either Authentic or Fake using a majority voting system based on the closest matching records.

---

## Presentation Video

Here are the links to our 8 minute YouTube presentation and live demo.

YouTube Part 1: https://youtu.be/DPa0IZRikPk

Backup YouTube Video Link 1: https://youtu.be/IDKRsvmWEMU

Backup YouTube Video Link 2: https://youtu.be/saRQJTyZVtU

If YouTube is not accessible, here is the Google Drive backup.

Google Drive: https://drive.google.com/file/d/1Iy0yXO3vUBpTFoS5SuMJmf6UoGnPMAPj/view?usp=sharing

---

## Dataset

You can download the medicine image dataset we used for testing from this link.

Dataset: https://share.google/lbp8nT0zpaVSA8HYj

Once downloaded, extract it and point the app to the folder using the Load Dataset button. Make sure the folder contains a Real subfolder and a Fake subfolder inside it.

---

## Project Folder Structure

The src folder holds all the Java source code and is split into these packages.

main holds the application entry point.
ui holds all the JavaFX interface components.
graph holds the Node, Edge, AdjacencyList, and Graph interface.
algorithms holds BFS, Dijkstra, KNNGraph, MST, Classification, and SimilarityDetection.
datastructures holds our custom HashMap, LinkedList, Stack, Queue, and Heap.
imageprocessing holds ColorHistogram, FeatureExtractor, and ImageToGraph.
model holds MedicineRecord and Database.

The dist folder holds the executable JAR file.
The ss folder holds the project slideshow.
The Logo folder holds the application logo image.

---

## What You Need To Run This

You need Java 17 or higher. We developed and tested on Java 25.
You also need JavaFX 21. If you do not have it download it from https://gluonhq.com/products/javafx and extract it somewhere on your computer like C:\javafx-sdk-21.

---

## How To Run Using The JAR File

Open a terminal in the dist folder and type this command. Make sure you replace the path with wherever you actually put your JavaFX folder.

java --module-path "C:\javafx-sdk-21\lib" --add-modules=javafx.base,javafx.controls,javafx.fxml,javafx.graphics,javafx.media -jar FakeMedicineDetection.jar

---

## How To Run From Source Code

Open the project in eclipse or any Java IDE that supports JavaFX.
In your run configuration add these VM options.

--module-path "C:\javafx-sdk-21\lib" --add-modules=javafx.base,javafx.controls,javafx.fxml,javafx.graphics,javafx.media

Set the main class to main.Main and then click run.

---

## How To Use The Application

When the app opens you will see the landing page. Click START to go into the main window.

From there click Load Dataset and select a folder that has a Real subfolder and a Fake subfolder inside it. Each folder should contain medicine images in jpg or png format. The app will read all the images and build the database.

Once that is done click Upload Image and pick the medicine photo you want to test. After that click the Analyse button and the app will compare your image to the database and show you the result on the right side of the screen.

We can Click a node to find more information about it and the node id. Then click Find Path and the app will highlight the cheapest route between them using Dijkstra. You can also tick the Show MST box to see the minimum spanning tree drawn on the graph.

The CRUD buttons at the bottom of the graph panel let you add nodes, remove nodes, update edges, and remove edges directly on the graph.

---

## Dataset Folder Format

Your dataset folder needs to have exactly this structure inside it.

A folder called Real that contains the genuine medicine images.
A folder called Fake that contains the counterfeit medicine images.

Supported file formats are jpg, jpeg, and png.

---

## Algorithms The System Uses

KNN Graph connects each image region to its 5 most colour-similar neighbours.
Cosine Similarity measures how alike two feature vectors are.
kNN Classification takes a majority vote from the 5 most similar database records to decide Authentic or Fake.
Dijkstra finds the shortest path between two selected nodes on the graph.
Kruskal builds the minimum spanning tree connecting all nodes with the lowest total edge weight.
BFS counts how many connected components exist in the graph.

---

## Extra Notes

The Logo folder must stay in the same directory as the JAR file for the logo to show up correctly on the landing page.
The application does not use the internet at all. Everything runs locally on your machine.
