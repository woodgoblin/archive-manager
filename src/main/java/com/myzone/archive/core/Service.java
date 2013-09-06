package com.myzone.archive.core;

/**
 * @author myzone
 * @date 9/6/13 10:21 AM
 */
public interface Service<A, R> {

    R process(A request, Core.ApplicationDataContext dataContext);

}
