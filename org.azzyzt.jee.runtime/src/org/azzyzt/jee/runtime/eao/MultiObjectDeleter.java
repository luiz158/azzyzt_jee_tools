package org.azzyzt.jee.runtime.eao;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.azzyzt.jee.runtime.exception.AccessDeniedException;
import org.azzyzt.jee.runtime.exception.EntityNotFoundException;
import org.azzyzt.jee.runtime.exception.InvalidArgumentException;
import org.azzyzt.jee.runtime.exception.InvalidIdException;
import org.azzyzt.jee.runtime.meta.TypeMetaInfoInterface;

/**
 * One of the service beans generated by Azzyzt JEE Tools, <code>ModifyMultiBean</code>, 
 * needs to deal with polymorphic lists of DTOs. In order to delete the associated 
 * entities, <code>ModifyMultiBean</code> uses a <code>MultiObjectDeleter</code>.
 * 
 * <code>MultiObjectDeleter</code> utilizes meta information generated by Azzyzt 
 * JEE Tools to find out about associated entity classes.
 * 
 * @see MultiObjectSaver
 * @see TypeMetaInfoInterface
 */
public class MultiObjectDeleter {
	
	/*
	 * No fields so far
	 */
	
	public void delete(
			EaoBase eao, 
			TypeMetaInfoInterface tmi, 
			@SuppressWarnings("rawtypes") List dtos
			)
	throws EntityNotFoundException, AccessDeniedException, InvalidArgumentException, 
	       InvalidIdException
	{
		for (Object dto : dtos) {
			Object id;
			try {
				Method idGetter = dto.getClass().getMethod("getId", (Class<?>[])null);
				id = idGetter.invoke(dto, (Object[])null);
			} catch (SecurityException e) {
				throw new AccessDeniedException();
			} catch (NoSuchMethodException e) {
				throw new InvalidArgumentException(dto.getClass().getName()+": no getId() found");
			} catch (IllegalArgumentException e) {
				throw new AccessDeniedException();
			} catch (IllegalAccessException e) {
				throw new AccessDeniedException();
			} catch (InvocationTargetException e) {
				throw new AccessDeniedException();
			}
			eao.delete(id, tmi.getEntityForDto(dto.getClass()));
		}
	}

}
