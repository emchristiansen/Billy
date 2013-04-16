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
assert(len(lines) == 5)
title = lines[0]
matchDistances0 = map(float, lines[1].split())
#matchDistances1 = map(float, lines[2].split())
#matchDistances2 = map(float, lines[3].split())
impostorDistances0 = map(float, lines[3].split())
#impostorDistances1 = map(float, lines[4].split())
#impostorDistances2 = map(float, lines[6].split())

from scipy import *
def numInRange(data, low, high):
    return len(filter(lambda x: x >= low and x < high, data))

def histogram(data):
    if len(data) == 0: return [], []
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
    return xs, ys
        

from matplotlib.ticker import LinearLocator, FormatStrFormatter, ScalarFormatter

def plotHistogram(data, label):
    x, y = histogram(data)
    plt.plot(x, y, label=label, antialiased=True)

#binWidth = 2
# xSame, ySame = histogram(sameDistances)
# print xSame
# print ySame
# xDiff, yDiff = histogram(differentDistances)
fig = plt.figure()
plotHistogram(matchDistances0, 'matches0')
#plotHistogram(matchDistances1, 'matches1')
#plotHistogram(matchDistances2, 'matches2')
plotHistogram(impostorDistances0, 'impostors0')
#plotHistogram(impostorDistances1, 'impostors1')
#plotHistogram(impostorDistances2, 'impostors2')

#plt.suptitle(wrapString(title))
plt.xlabel("distance")
formatter = ScalarFormatter()
formatter.set_powerlimits((2, 2))
fig.gca().xaxis.set_major_formatter(formatter)
#fig.gca().xaxis.set_major_formatter(FormatStrFormatter('%.02f'))
#fig.gca().yaxis.set_major_formatter(FormatStrFormatter('%.02f'))
plt.ylabel("frequency")
plt.legend(loc='upper left')
plt.savefig(outputFile, bbox_inches='tight')
