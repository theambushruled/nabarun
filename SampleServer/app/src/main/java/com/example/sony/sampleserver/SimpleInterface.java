package com.example.sony.sampleserver;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.annotation.BusInterface;
import org.alljoyn.bus.annotation.BusSignal;

/*
 * The BusInterface annotation is used to tell the code that this interface is an AllJoyn interface.
 *
 * The 'name' value is used to specify by which name this interface will be known.  If the name is
 * not given the fully qualified name of the Java interface is be used.  In most instances its best
 * to assign an interface name since it helps promote code reuse.
 */
@BusInterface(name = "org.alljoyn.bus.test.sessions")
public interface SimpleInterface {

    /*
     * The BusMethod annotation signifies that this function should be used as part of the AllJoyn
     * interface.  The runtime is smart enough to figure out what the input and output of the method
     * is based on the input/output arguments of the Ping method.
     *
     * All methods that use the BusMethod annotation can throw a BusException and should indicate
     * this fact.
     */
    @BusSignal
    public void coords(int x, int y) throws BusException;
}