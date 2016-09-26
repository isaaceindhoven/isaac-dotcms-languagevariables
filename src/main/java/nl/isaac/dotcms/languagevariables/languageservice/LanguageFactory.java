package nl.isaac.dotcms.languagevariables.languageservice;


public class LanguageFactory {
		
	
//	/** 
//	 * 
//	 * @param languageId if null then all the contentlets will be returned
//	 * @return
//	 */
//		List<Contentlet> getAllContentlets(Long languageId) {
//			Long startTime = System.currentTimeMillis();
//			User user1=getUser();
//			
//			String luceneQuery = "";
//			//archived = +deleted:true
//			if(null != languageId) {
//				luceneQuery = "+structureName:"+Configuration.getStructureVelocityVarName()+" +(conhost:"+getHostIdentifier()+") +languageId:" + languageId + " +working:true";
//			} else {			
//				luceneQuery = "+structureName:"+Configuration.getStructureVelocityVarName()+" +(conhost:"+getHostIdentifier()+")";
//			}
//			List<Contentlet> contentlets = null;
//			try {
//				contentlets = APILocator.getContentletAPI().search(luceneQuery, 0, 0, "", user1, false);
//				Collections.sort(contentlets, new LanguageKeyComparator());
//			} catch (DotDataException e) {
//				throw new RuntimeException(e.toString(), e);
//			} catch (DotSecurityException e) {
//				throw new RuntimeException(e.toString(), e);
//			}
//			
//			Long end = System.currentTimeMillis() - startTime;
//			Logger.info(this, "Get Contentlets time: " + end  + "ms , size: " + contentlets.size());
//			return contentlets;
//		}
//	
//	/** 
//	 * 
//	 * @param languageId if null then all the contentlets will be returned
//	 * @return
//	 */
//	List<Key> getAllKeyObjects(Long languageId) {
//		Long startTime = System.currentTimeMillis();
//		User user1=getUser();
//		String hostIdentifier = getHostIdentifier();
//			
//		String luceneQueryUnpublished = "+structureName:"+Configuration.getStructureVelocityVarName()+" +(conhost:"+hostIdentifier+") +working:true +live:false";
//		String luceneQueryLocked = "+structureName:"+Configuration.getStructureVelocityVarName()+" +(conhost:"+hostIdentifier+") +locked:true +working:true";
//		String luceneQueryArchived = "+structureName:"+Configuration.getStructureVelocityVarName()+" +(conhost:"+hostIdentifier+") +deleted:true +working:true";
//		String luceneQuery = "+structureName:"+Configuration.getStructureVelocityVarName()+" +(conhost:"+hostIdentifier+") +working:true";
//
//		//If languageId is filled, add languageId to query
//		if(null != languageId) {
//			luceneQuery += " +languageId:" + languageId;
//			luceneQueryArchived += " +languageId:" + languageId;
//			luceneQueryLocked += " +languageId:" + languageId;
//			luceneQueryUnpublished += " +languageId:" + languageId;
//		}
//		
//		List<Contentlet> contentlets = null;
//		List<Contentlet> contentletsLocked = null;
//		List<Contentlet> contentletsArchived = null;
//		List<Contentlet> contentletsUnpublished = null;
//		
//		try {
//			contentlets = APILocator.getContentletAPI().search(luceneQuery, 0, 0, "", user1, false);
//			contentletsArchived = APILocator.getContentletAPI().search(luceneQueryArchived, 0, 0, "", user1, false);
//			contentletsLocked = APILocator.getContentletAPI().search(luceneQueryLocked, 0, 0, "", user1, false);
//			contentletsUnpublished = APILocator.getContentletAPI().search(luceneQueryUnpublished, 0, 0, "", user1, false);
//			
//			//Order list by key value
//			Collections.sort(contentlets, new LanguageKeyComparator());
//		} catch (DotDataException e) {
//			throw new RuntimeException(e.toString(), e);
//		} catch (DotSecurityException e) {
//			throw new RuntimeException(e.toString(), e);
//		}
//			
//		Set<Contentlet> set = new LinkedHashSet<Contentlet>(contentlets);
//		Set<Contentlet> setArchived = new LinkedHashSet<Contentlet>(contentletsArchived);
//		Set<Contentlet> setLocked = new LinkedHashSet<Contentlet>(contentletsLocked);
//		Set<Contentlet> setUnpublished = new LinkedHashSet<Contentlet>(contentletsUnpublished);
//		
//		List<Key> result = getKeyObjects(set, setArchived, setLocked, setUnpublished);
//			
//		Long end = System.currentTimeMillis() - startTime;
//		Logger.info(this, "Get Contentlets time: " + end  + "ms");
//		
//		return result;
//	}
//	
//	/**
//	 * 
//	 * @param allC
//	 * @param archivedC
//	 * @param lockedC
//	 * @return
//	 */
//	List<Key> getKeyObjects(Set<Contentlet> allC, Set<Contentlet> archivedC, Set<Contentlet> lockedC, Set<Contentlet> unpublishedC) {
//		List<Key> result = new ArrayList<Key>();
//		List<LanguageValue> translations = new ArrayList<LanguageValue>();
//		String oldKey = null;
//				
//		// Iterate over this list and transform it into a new list of Key objects. Because we
//		// know that the given list if ordered by key we can do a "one item look ahead"
//		Iterator<Contentlet> it = allC.iterator();
//		Logger.info(this, "set: " + allC.size());
//        while (it.hasNext()) {
//        	
//        	Contentlet c = it.next();
//        	String newKey = c.getStringProperty("key");
//
//        	if (null == oldKey || !newKey.equals(oldKey)) {
//				translations = new ArrayList<LanguageValue>();
//				try {
//					
//					boolean archived = false;
//					boolean locked = false;
//					boolean published = true;
//					
//					if(archivedC.contains(c)) {
//		    			Logger.info(this, "Contentlet Archived!");
//		    			archived = true;
//		    		} 
//		    			
//					if(unpublishedC.contains(c)) {
//						Logger.info(this, "Contentlet Unpublished!");
//						published = false;
//					}
//					
//		    		if(lockedC.contains(c)) {
//		    			Logger.info(this, "Contentlet Locked!");
//		    			locked = true;
//		    		} 
//		    			
//		    		oldKey = newKey;
//					result.add(new Key(newKey, published, archived, locked, translations));
//				} catch (DotStateException e) {
//					throw new RuntimeException(e);
//				} catch (DotRuntimeException e) {
//					throw new RuntimeException(e);
//				} 
//			}
//			translations.add(new LanguageValue(String.valueOf(c.getLanguageId()), c.getStringProperty("value"))); 
//        } 		
//		return result;
//	}
//	
//	 String getHostIdentifier() {
//		 	HttpServletRequest request = RequestStoringFilter.request.get();
//		 	HostWebAPIImpl hostWebApiImpl = new HostWebAPIImpl();
//		 	Host host = null;
//		 	
//		 	try {
//		 		host = hostWebApiImpl.getCurrentHost(request);
//			} catch (PortalException e) {
//				throw new RuntimeException(e.toString(), e);
//			} catch (SystemException e) {
//				throw new RuntimeException(e.toString(), e);
//			} catch (DotDataException e) {
//				throw new RuntimeException(e.toString(), e);
//			} catch (DotSecurityException e) {
//				throw new RuntimeException(e.toString(), e);
//			}
//		 	
//		 	Logger.info(this, "Host Identifier: " + host.getIdentifier());
//		 	return host.getIdentifier();
//	 }
//	 
//	 Contentlet createNewContentlet(Long languageId, String key, String value) {
//		 Structure s = StructureCache.getStructureByVelocityVarName(Configuration.getStructureVelocityVarName());
//		 Contentlet contentlet = new Contentlet();
//		 contentlet.setHost(getHostIdentifier());	
//		 contentlet.setLanguageId(languageId);
//		 contentlet.setInode("");
//		 contentlet.setStructureInode(s.getInode());
//		 contentlet.setStringProperty("key", key);
//		 contentlet.setStringProperty("value", value);
//		 try {
//			return APILocator.getContentletAPI().checkin(contentlet, getUser(), false);
//		} catch (DotContentletValidationException e) {
//			Logger.error(this, e.toString(), e);
//			throw new RuntimeException(e.toString(), e);
//		} catch (DotContentletStateException e) {
//			Logger.error(this, e.toString(), e);
//			throw new RuntimeException(e.toString(), e);
//		} catch (IllegalArgumentException e) {
//			Logger.error(this, e.toString(), e);
//			throw new RuntimeException(e.toString(), e);
//		} catch (DotDataException e) {
//			Logger.error(this, e.toString(), e);
//			throw new RuntimeException(e.toString(), e);
//		} catch (DotSecurityException e) {
//			Logger.error(this, e.toString(), e);
//			throw new RuntimeException(e.toString(), e);
//		}
//	 }
//	 
//	 /**
//	 * Get current dotCMS User based on request
//	 * @return User
//	 */
//	User getUser() {
//		try {
//			return PortalUtil.getUser(RequestStoringFilter.request.get());
//		} catch (PortalException e1) {
//			throw new RuntimeException(e1.toString(), e1);
//		} catch (SystemException e1) {
//			throw new RuntimeException(e1.toString(), e1);
//		}
//	}
}
