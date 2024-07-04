import numpy as np
from scipy.io import loadmat, savemat
from pynndescent import NNDescent
import numba

data = np.zeros((0, 384))

for i in range(100):
    data = np.vstack((data, loadmat(f'/data/mf_dino2/{i}.mat')['features']))

# sm with t=1
t = 10
sm_data = np.exp(data / t)/np.sum( np.exp(data / t), axis=1, keepdims=True )

@numba.jit
def SED(x, y):
    return ( C( (x + y) / 2) / np.sqrt(C(x) * C(y)) ) - 1

@numba.jit
def H(x):
    return -np.sum(x * np.log(x))

@numba.jit
def C(x):
    return np.exp(H(x))

sed_nns = NNDescent(sm_data, metric=SED, n_neighbors=101)

savemat(f'mf_dino2_sed_nns_sm_t{t}.mat', {'indexes': sed_nns.neighbor_graph[0], 'distances': sed_nns.neighbor_graph[1]})
