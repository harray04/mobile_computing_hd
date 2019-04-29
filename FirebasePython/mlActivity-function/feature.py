import siganalysis as sa
import numpy as np
from numpy import convolve
import matplotlib.pyplot as plt
from scipy.interpolate import spline
import math
from scipy.fftpack import *
import scipy

def plot_m(m):
    plt.plot(m)
    plt.show()

def smoothen_values(fileName):
    x, y, z, ty = np.loadtxt(fileName, delimiter =',', unpack =True)
    mpre = x*x+y*y+z*z
    m = np.sqrt(mpre)
    m_smooth = np.zeros(len(m))
    m_smooth = m


    for i ,val in enumerate(m_smooth[2 : (len(m_smooth) -2)]):
        m_smooth[i] = (m_smooth[i-2] + m_smooth[i-1] +m_smooth[i] + m_smooth[i+1] + m_smooth[i+2])/5
        m = m_smooth
    return m

def stft(x, fftsize=256, overlap=2):   
    hop = round(fftsize / overlap)
    w = scipy.hanning(fftsize+1)[:-1]
    return np.array([np.fft.rfft(w*x[i:i+fftsize]) for i in range(0, len(x)-fftsize, hop)])

def calc_energy(m):
    stft_signal = stft(m)
    energy_signal = []

    for i, amp in enumerate(stft_signal):
        energy_signal.append(np.sum(np.power(abs(stft_signal[i]),2)))
    return energy_signal




def feature(x, fftsize=256, overlap=2):
    meanamp = []
    maxamp = []
    minamp = []
    stdamp = []
    energyamp = []
    hop = round(fftsize / overlap)
    for i in range(0, len(x)-fftsize, hop):
        meanamp.append(np.array(np.mean(x[i:i+fftsize])))
        maxamp.append(np.array(np.max(x[i:i+fftsize])))
        minamp.append(np.array(np.min(x[i:i+fftsize])))
        stdamp.append(np.array(np.std(x[i:i+fftsize])))
        energyamp.append(np.array(np.sum(np.power(x[i:i+fftsize],2))))
             
                  
    return meanamp ,maxamp ,minamp,stdamp,energyamp


 

#diff = np.subtract(valmax,valmin)

def smooth_plot(m):
    plt.plot(m)
    plt.xlabel('Freq')
    plt.ylabel('Energy')
    plt.title('Walking')
    plt.show()

def normalize_features(m):
    energy_signal = calc_energy(m)
    valmean, valmax, valmin, valstd, valenergy = feature(m)

    valmean_nor = ((valmean) - min(valmean))/(max(valmean) - min(valmean))
    valmax_nor = ((valmax) - min(valmax))/(max(valmax) - min(valmax))
    valmin_nor = ((valmin) - min(valmin))/(max(valmin) - min(valmin))
    valstd_nor = ((valstd) - min(valstd))/(max(valstd) - min(valstd))
    valenergy_nor = ((valenergy) - min(valenergy))/(max(valenergy) - min(valenergy))
    energy_signal_nor = ((energy_signal) - min(energy_signal))/(max(energy_signal) - min(energy_signal))

    return valmean_nor, valmax_nor, valmin_nor, valstd_nor, valenergy_nor, energy_signal_nor
def write_to_file(srcFileName, ty):
    m = smoothen_values(srcFileName)
    #smooth_plot(m)
    
    valmean, valmax, valmin, valstd, valenergy, energy_signal = normalize_features(m)
    for i ,val in enumerate(valmean):
        saveFile = open ('./features_final.csv','a')
        saveFile.write(str(valmean[i]) + ',' +str(valmax[i]) + ',' + str(valmin[i]) + ',' + str(valstd[i]) + ',' + str(valenergy[i])+','+ str(energy_signal[i]) + ',' + str(ty) )
        saveFile.write('\n')