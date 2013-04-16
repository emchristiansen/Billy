import sys
import matplotlib as mpl
mpl.use('Agg')
import matplotlib.pyplot as plt

def wrapString(s):
    maxLen = 80
    if len(s) <= maxLen:
        return s
    else:
        return s[:maxLen] + "\n" + wrapString(s[maxLen:])

# functionName inputFile outputFile.
assert(len(sys.argv) == 3)

inputFile = sys.argv[1]
outputFile = sys.argv[2]

lines = open(inputFile).readlines()
lines = [l.replace("\n", "") for l in lines]

# The first line is the graph title.
# 2nd: distances between same points
# 3rd: distances between different points
assert(len(lines) == 7)
title = lines[0]
l0Same = map(float, lines[1].split())
l0Different = map(float, lines[2].split())
cayleySame = map(float, lines[3].split())
cayleyDifferent = map(float, lines[4].split())
ktSame = map(float, lines[5].split())
ktDifferent = map(float, lines[6].split())

from scipy import *
def numInRange(data, low, high):
    return len(filter(lambda x: x >= low and x < high, data))

def histogram(data):
    start = min(data)
    stop = max(data)
    step = (stop - start) / 20.0
    halfstep = step / 2.0
    xs = [start - halfstep]
    ys = [0]
    for x in arange(start, stop, step):
        xs.append(x + halfstep)
        ys.append(numInRange(data, x, x + step))
    xs.append(stop + step)
    ys.append(0)
    ys = array(ys).astype(double) / array(ys).sum()
    xs = array(xs).astype(double)
    xs -= xs.min()
    xs /= xs.max()
    return xs, ys
        

from matplotlib.ticker import LinearLocator, FormatStrFormatter, ScalarFormatter

def plotHistogram(data, label, color):
    x, y = histogram(data)
    plt.plot(x, y, label=label, color=color, antialiased=True)

fig = plt.figure()
plotHistogram(l0Same, 'l0 match', '#0000ff')
plotHistogram(l0Different, 'l0 mismatch', '#000088')
#plotHistogram(cayleySame, 'cayley match', '#00ff00')
#plotHistogram(cayleyDifferent, 'cayley mismatch', '#008800')
plotHistogram(ktSame, 'kt match', '#ff0000')
plotHistogram(ktDifferent, 'kt mismatch', '#880000')

#plt.suptitle(wrapString(title))
plt.xlabel("normalized distance")
formatter = ScalarFormatter()
formatter.set_powerlimits((2, 2))
fig.gca().xaxis.set_major_formatter(formatter)
#fig.gca().xaxis.set_major_formatter(FormatStrFormatter('%.02f'))
#fig.gca().yaxis.set_major_formatter(FormatStrFormatter('%.02f'))
plt.ylabel("frequency")
plt.legend(loc='upper left')
plt.savefig(outputFile, bbox_inches='tight')
