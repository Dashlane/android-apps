package com.dashlane.util.exceptions

class NetworkOffException(exception: Exception? = null) :
    Exception("The network is not connected on this device", exception)