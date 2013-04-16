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
assert(len(lines) == 3)
title = lines[0]
sameDistances = map(float, lines[1].split())
differentDistances = map(float, lines[2].split())

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
    return xs, ys
        

# import random
# random.shuffle(differentDistances)
# differentDistances = differentDistances[:len(sameDistances)]

from matplotlib.ticker import LinearLocator, FormatStrFormatter, ScalarFormatter

# nSame, binsSame, _ = plt.hist(sameDistances, normed=1)
# nDifferent, binsDifferent, _ = plt.hist(differentDistances, normed=1)

# plt.plot(binsSame, nSame, label='matches', antialiased=True)

# print nSame
# print binsSame
# print patchesSame

# assert(false)

# n, _, _ = plt.hist((sameDistances, differentDistances), label=('matches', 'mismatches'), antialiased=True, linestyle='solid', linewidth=1, histtype='step')

#binWidth = 2
xSame, ySame = histogram(sameDistances)
print xSame
print ySame
xDiff, yDiff = histogram(differentDistances)
fig = plt.figure()
plt.plot(xSame, ySame, label='matches', antialiased=True)
plt.plot(xDiff, yDiff, label='mismatches', antialiased=True)

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
