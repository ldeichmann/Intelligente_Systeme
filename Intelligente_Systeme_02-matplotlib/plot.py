import matplotlib
matplotlib.use('Qt5Agg') # <-- THIS MAKES IT FAST!
from mpl_toolkits.mplot3d import Axes3D
from matplotlib import cm
import matplotlib.pyplot as plt
import numpy as np

fig = plt.figure()
ax = fig.add_subplot(111,projection='3d')

data = np.genfromtxt('data0.csv', delimiter = ',')
data2 = np.genfromtxt('label0.csv', delimiter = ',')

x = range(data.shape[1])
y = range(data.shape[0])
Z = data

x1 = data2[:,0]
y1 = data2[:,1]

X, Y = np.meshgrid(x, y)

surf = ax.plot_surface(X, Y, Z, rstride=25, cstride=25, cmap=cm.Greys,
                       linewidth=1, antialiased=True)

a = []
for j in range(len(x1)):
        a.append(data[y1[j]][x1[j]])


ax.scatter(x1, y1, a, marker="x", c='r')

ax.set_zlim3d(np.min(Z), np.max(Z))
fig.colorbar(surf)

plt.show()