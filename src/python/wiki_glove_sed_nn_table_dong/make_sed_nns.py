import numpy as np
from scipy.io import loadmat, savemat
from pynndescent import NNDescent
import numba

t=100

data = loadmat(f'/data/wikipedia_glove/wiki_glove_100d.mat')['glove']

# L2 normalise
data = data / np.linalg.norm(data, axis=1, keepdims=True)

print(data.shape)

# sm with t=1
sm_data = np.exp(data / t)/np.sum( np.exp(data / t), axis=1, keepdims=True )

savemat('sm_glove.mat', {'features': sm_data})

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

savemat(f'wiki_glove_sed_nns_sm_t{t}.mat', {'indexes': sed_nns.neighbor_graph[0], 'distances': sed_nns.neighbor_graph[1]})
np.savetxt(f"wiki_glove_sed_nns_sm_t{t}_dists.txt", sed_nns.neighbor_graph[1], delimiter=' ', fmt='%1.3f')
np.savetxt(f"wiki_glove_sed_nns_sm_t{t}_indices.txt", sed_nns.neighbor_graph[0], delimiter=' ', fmt='%1.3f')
