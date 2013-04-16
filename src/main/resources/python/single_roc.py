import sys
import matplotlib as mpl
mpl.use('Agg')
import matplotlib.pyplot as plt

# functionName inputFile outputFile.
assert(len(sys.argv) == 3)

inputFile = sys.argv[1]
outputFile = sys.argv[2]

lines = open(inputFile).readlines()
lines = [l.replace("\n", "") for l in lines]

# The first line is the graph title, subsequent lines are groups of {curveName, falsePostiveRate, truePositiveRate}.
assert(not ((len(lines) - 1) % 3))

title = lines[0]

def group(n, xs):
    if len(xs) == 0:
        return []
    else:
        return [xs[0 : n]] + group(n, xs[n :])

nameTPRFPRs = group(3, lines[1 :])
  
markers = [7, 4, 5, 6, '8', 'p', 'H']
markerInd = 0

def addROCCurve(name, fprs, tprs):
    toFloat = lambda s: float(s)

    fprs = map(toFloat, fprs.split())
    tprs = map(toFloat, tprs.split())

    global markerInd
    marker = markers[markerInd]
    markerInd += 1
    print marker
    plt.plot(fprs, tprs, label=name, antialiased=True, marker=marker, markevery=500, markersize=5)

def wrapString(s):
    maxLen = 80
    if len(s) <= maxLen:
        return s
    else:
        return s[:maxLen] + "\n" + wrapString(s[maxLen:])

def rocFigure(title, curves, outPath):
    plt.figure()
#    plt.suptitle(wrapString(title))
    plt.xlabel("false positive rate")
    plt.ylabel("true positive rate")
    plt.axis([0, 1, 0, 1])
    
    for curve in curves: addROCCurve(*curve)

    plt.legend(loc='lower right')

    plt.setp(plt.gca().get_legend().get_texts(), fontsize='small')
    plt.grid(True)

    plt.axes().set_aspect('equal')
    plt.xlim(0, 0.71)

    plt.savefig(outPath, bbox_inches='tight')

rocFigure(title, nameTPRFPRs, outputFile)
