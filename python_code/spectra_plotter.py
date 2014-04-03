#!/usr/bin/env python

#########################################
####         spectra_plotter         ####
#### Nic Ross and Victoria Tielebein ####
####   Reads and plots a fit file.   ####
####        Created: 11/1/2013       ####
####     Last Updated: 11/7/2013     ####
#########################################

import numpy
import pyfits
import matplotlib
from pylab import *

## TODO This needs to be passed in from the Bash script optimally or something
WORKING_DIRECTORY = "DownloadCenter/bin/"
DOWNLOAD_DIRECTORY = WORKING_DIRECTORY + "downloads/"
TABLE_NAME = "QuasarSpectraTable.qst"

def importDatabase():
    fin = open(DOWNLOAD_DIRECTORY + TABLE_NAME, 'r')

    for line in fin:
        fin.
    
    fin.close();
    

def importFitFile(filename):
    data = pyfits.open(filename)

    spectra = data[0]

    c0 = spectra.header['COEFF0']
    c1 = spectra.header['COEFF1']

    n = len( spectra.data[0] )

    #select x-values by calculating wavelength
    wavelength = pow( 10, c0 + c1*numpy.arange(n) )

    #select flux for y-values
    flux = spectra.data[0]

    return wavelength,flux

def calculateRatio(flux1, flu2):
    ratio = flux1 / flux2
    return ratio

## main ##
wavelength1, flux1 = importFitFile(file1)
wavelength2, flux2 = importFitFile(file2)

## TODO smoothing (plot average of 3 px instead of each or something)

## TODO in its own frame somehow
plot(wavelength1, calulateRatio(flux1, flux2) )

## TODO many frames in the same window with a "next" option -- load plate
## one at a time and open all duplicates on that plate. when end of plate
## ask if next plate should be opened

#create graph and plot
figure()
plot(wavelength1, flux1)
plot(wavelength2, flux2)
xlabel('wavelength, Angstroms')
ylabel('flux, 10^(-17) erg/(cm*s^2*Angstroms)')
title(file1 +" and " + file2)

show()
