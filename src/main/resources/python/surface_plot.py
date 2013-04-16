import sys
from mpl_toolkits.mplot3d import Axes3D
import matplotlib as mpl
mpl.use('Agg')
import matplotlib.pyplot as plt

from scipy import *

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
# 2nd: blurWidth
# 3rd: patchSize
# 4th: recognitionRate
assert(len(lines) == 4)
title = lines[0]
blurWidths = map(float, lines[1].split())
patchSizes = map(float, lines[2].split())
recognitionRates = map(float, lines[3].split())

uniqueBlurs = sort(list(set(blurWidths)))
numBlurs = len(uniqueBlurs)
blurToIndex = dict(zip(uniqueBlurs, range(numBlurs)))

uniqueSizes = sort(list(set(patchSizes)))
numSizes = len(uniqueSizes)
sizeToIndex = dict(zip(uniqueSizes, range(numSizes)))

(X, Y) = meshgrid(uniqueBlurs, uniqueSizes)
Z = zeros(X.shape)
for i in range(len(recognitionRates)):
    b = blurWidths[i]
    s = patchSizes[i]
    r = recognitionRates[i]
    bInd = blurToIndex[b]
    sInd = sizeToIndex[s]
    Z[sInd, bInd] = r

print X
print Y
print Z

print uniqueBlurs
print uniqueSizes
print recognitionRates

from matplotlib import cm
from matplotlib.ticker import LinearLocator, FormatStrFormatter

fig = plt.figure()
ax = fig.gca(projection='3d')
#surf = ax.plot_surface(X, Y, Z, rstride=1, cstride=1, cmap=cm.jet, linewidth=0, antialiased=True)
surf = ax.plot_surface(X, Y, Z, rstride=1, cstride=1, cmap=cm.jet, antialiased=True, alpha=.8)
ax.set_zlim(0, 1)
ax.set_xlabel('blur width')
ax.set_ylabel('patch width')
ax.set_zlabel('recognition rate')
# cset = ax.contourf(X, Y, Z, zdir='z', offset=0)
# cset = ax.contourf(X, Y, Z, zdir='x', offset=0)
# cset = ax.contourf(X, Y, Z, zdir='y', offset=0)

ax.zaxis.set_major_locator(LinearLocator(10))
ax.zaxis.set_major_formatter(FormatStrFormatter('%.02f'))

#fig.colorbar(surf, shrink=0.5, aspect=5)

# ax.plot_surface(X, Y, Z, cmap=cm.jet)
# plt.suptitle(wrapString(title))
plt.savefig(outputFile, bbox_inches='tight')
