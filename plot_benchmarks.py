import argparse
import os
import sys
from pathlib import Path

import matplotlib.pyplot as plt
import pandas as pd
import seaborn as sns

"""
Plot benchmark results from multiple files.

Usage:
    python plot_benchmarks.py file1.csv file2.csv ...

The filename (without extension) will be used as the system name in the plots.
For example, 'amd64.csv' will be labeled as 'amd64' in the plots.

Output:
    - PNG charts in the 'docs/benchmark_plots' directory
    - Text summary of benchmark results
"""

# Parse command line arguments
parser = argparse.ArgumentParser(description='Plot benchmark results from multiple files.')
parser.add_argument('benchmark_files', nargs='+', help='Benchmark files to process (*.csv)')
args = parser.parse_args()

# Function to parse benchmark file
def parse_benchmark_file(file_path, system_name):
    bench_df = pd.read_csv(file_path)
    bench_df['System'] = system_name
    return bench_df

# Parse all benchmark files
dfs = []
for benchmark_file in args.benchmark_files:
    # Check if file exists
    if not os.path.isfile(benchmark_file):
        print(f"Error: File '{benchmark_file}' does not exist. Skipping.", file=sys.stderr)
        continue

    # Extract system name from filename (without extension)
    system_name = Path(benchmark_file).stem
    print(f"Processing {benchmark_file} as system '{system_name}'")

    try:
        # Parse the file
        dfs.append(parse_benchmark_file(benchmark_file, system_name))
    except Exception as e:
        print(f"Error processing file '{benchmark_file}': {e}", file=sys.stderr)

# Check if we have any data to process
if not dfs:
    print("No valid benchmark data found. Exiting.", file=sys.stderr)
    sys.exit(1)

df = pd.concat(dfs).reset_index(drop=True)
df['BenchmarkType'] = df['Benchmark'].str.split('.').str[-2]
df['Implementation'] = df['Benchmark'].str.split('.').str[-1]
print(df.columns)

# Create output directory for PNG files
os.makedirs('docs/benchmark_plots', exist_ok=True)

print("\nGenerating benchmark plots...")

# Process each benchmark type
for benchmark_type, data in df.groupby('BenchmarkType'):
    # Get unique array sizes
    array_sizes = sorted(data['Param: arraySize'].unique())

    # Create a new figure for each benchmark
    num_sizes = len(array_sizes)
    fig_width = max(12, num_sizes * 3)
    plt.figure(figsize=(fig_width, 4), dpi=300)

    # Create subplots for each array size in a single row
    for i, array_size in enumerate(array_sizes):
        # Create subplot in a single row
        plt.subplot(1, num_sizes, i + 1)

        # Create barplot with system as hue
        sns.barplot(
            data=(data[data['Param: arraySize'] == array_size]),
            x='Implementation',
            y='Score',
            hue='System',
        )

        # Set up the subplot
        plt.title(f"Array Size: {array_size}")
        plt.yscale('log')  # Logarithmic scale for y-axis
        plt.ylabel('Score (ops/s)')
        plt.xticks(rotation=45)
        plt.legend(title='System')
        plt.grid(True, which="both", ls="-", alpha=0.2)
        # Fix y-axis orientation (ensure lowest at bottom, highest at top)
        plt.gca().invert_yaxis()

    # Save the plot as PNG
    plt.tight_layout()
    plt.savefig(f"docs/benchmark_plots/{benchmark_type}.png")
    plt.close()

    print(f"Created: docs/benchmark_plots/{benchmark_type}.png")


print("\nPNG charts have been created in the 'docs/benchmark_plots' directory.")
