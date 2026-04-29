/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2021 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.journal;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.DeviceCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

public enum Document {

	ADVENTURERS_GUIDE(ItemSpriteSheet.GUIDE_PAGE, false),
	ALCHEMY_GUIDE(ItemSpriteSheet.ALCH_PAGE, false),
	LORE_PLACEHOLDER(ItemSpriteSheet.SOMETHING, true);

	Document( int sprite ){
		this(sprite, false);
	}

	Document( int sprite, boolean lore ){
		pageSprite = sprite;
		loreDocument = lore;
	}

	public static final int NOT_FOUND = 0;
	public static final int FOUND     = 1;
	public static final int READ      = 2;

	private LinkedHashMap<String, Integer> pagesStates = new LinkedHashMap<>();

	public Collection<String> pages(){
		return pagesStates.keySet();
	}

	// Sets page to FOUND if currently NOT_FOUND. Returns true if state changed.
	public boolean findPage( String page ) {
		if (pagesStates.containsKey(page) && pagesStates.get(page) == NOT_FOUND) {
			pagesStates.put(page, FOUND);
			Journal.saveNeeded = true;
			return true;
		}
		return false;
	}

	// Alias kept for DocumentPage.java call site.
	public boolean addPage( String page ) {
		return findPage(page);
	}

	// Sets page to READ if currently FOUND or READ. Returns true if page is found.
	public boolean readPage( String page ) {
		if (pagesStates.containsKey(page) && pagesStates.get(page) >= FOUND) {
			if (pagesStates.get(page) != READ) {
				pagesStates.put(page, READ);
				Badges.validateCatalogBadges();
				Journal.saveNeeded = true;
			}
			return true;
		}
		return false;
	}

	public boolean readPage( int pageIdx ) {
		return readPage( pagesStates.keySet().toArray(new String[0])[pageIdx] );
	}

	public boolean deletePage( String page ) {
		if (pagesStates.containsKey(page)) {
			pagesStates.put(page, NOT_FOUND);
			Journal.saveNeeded = true;
			return true;
		}
		return false;
	}

	public boolean unreadPage( String page ) {
		if (pagesStates.containsKey(page) && pagesStates.get(page) == READ) {
			pagesStates.put(page, FOUND);
			Journal.saveNeeded = true;
			return true;
		}
		return false;
	}

	public boolean hasPage( String page ){
		return pagesStates.containsKey(page) && pagesStates.get(page) >= FOUND;
	}

	public boolean hasPage( int pageIdx ){
		return hasPage( pagesStates.keySet().toArray(new String[0])[pageIdx] );
	}

	public boolean hasAnyPages(){
		for (int state : pagesStates.values()){
			if (state >= FOUND) return true;
		}
		return false;
	}

	public boolean anyPagesFound(){
		return hasAnyPages();
	}

	public boolean anyPagesRead(){
		for (int state : pagesStates.values()){
			if (state == READ) return true;
		}
		return false;
	}

	public boolean isPageFound( String page ){
		return pagesStates.containsKey(page) && pagesStates.get(page) >= FOUND;
	}

	public boolean isPageFound( int pageIdx ){
		return isPageFound( pagesStates.keySet().toArray(new String[0])[pageIdx] );
	}

	public boolean isPageRead( String page ){
		return pagesStates.containsKey(page) && pagesStates.get(page) == READ;
	}

	public boolean isPageRead( int pageIdx ){
		return isPageRead( pagesStates.keySet().toArray(new String[0])[pageIdx] );
	}

	public List<String> pageNames(){
		return new ArrayList<>(pagesStates.keySet());
	}

	public int pageIdx( String page ){
		int i = 0;
		for (String p : pagesStates.keySet()){
			if (p.equals(page)) return i;
			i++;
		}
		return -1;
	}

	private int pageSprite;
	public int pageSpriteIdx(){
		return pageSprite;
	}

	public Image pageSprite(){
		return new ItemSprite(pageSprite);
	}

	public Image pageSprite( String page ){
		return new ItemSprite(pageSprite);
	}

	private boolean loreDocument;
	public boolean isLoreDoc(){
		return loreDocument;
	}

	public String discoverHint( String page ){
		String hint = Messages.get( this, name() + "." + page + ".discover_hint" );
		if (hint.equals("!!!NO TEXT FOUND!!!")){
			hint = Messages.get( Document.class, "discover_hint_generic" );
		}
		return hint;
	}

	public String discoverHint(){
		String hint = Messages.get( this, name() + ".discover_hint" );
		if (hint.equals("!!!NO TEXT FOUND!!!")){
			hint = Messages.get( Document.class, "discover_hint_generic" );
		}
		return hint;
	}

	public String title(){
		return Messages.get( this, name() + ".title");
	}

	public String pageTitle( String page ){
		return Messages.get( this, name() + "." + page + ".title");
	}

	public String pageTitle( int pageIdx ){
		return pageTitle( pagesStates.keySet().toArray(new String[0])[pageIdx] );
	}

	public String pageBody( String page ){
		return Messages.get( this, name() + "." + page + ".body");
	}

	public String pageBody( int pageIdx ){
		return pageBody( pagesStates.keySet().toArray(new String[0])[pageIdx] );
	}

	public static final String GUIDE_INTRO_PAGE = "Intro";
	public static final String GUIDE_SEARCH_PAGE = "Examining_and_Searching";

	static {
		int debugState = DeviceCompat.isDebug() ? READ : NOT_FOUND;

		ADVENTURERS_GUIDE.pagesStates.put(GUIDE_INTRO_PAGE, 	debugState);
		ADVENTURERS_GUIDE.pagesStates.put("Identifying", 		debugState);
		ADVENTURERS_GUIDE.pagesStates.put(GUIDE_SEARCH_PAGE, 	debugState);
		ADVENTURERS_GUIDE.pagesStates.put("Strength", 		debugState);
		ADVENTURERS_GUIDE.pagesStates.put("Food", 			debugState);
		ADVENTURERS_GUIDE.pagesStates.put("Levelling", 		debugState);
		ADVENTURERS_GUIDE.pagesStates.put("Surprise_Attacks", debugState);
		ADVENTURERS_GUIDE.pagesStates.put("Dieing", 			debugState);
		ADVENTURERS_GUIDE.pagesStates.put("Looting", 		    debugState);
		ADVENTURERS_GUIDE.pagesStates.put("Magic", 			debugState);

		//sewers
		ALCHEMY_GUIDE.pagesStates.put("Potions",              debugState);
		ALCHEMY_GUIDE.pagesStates.put("Stones",               debugState);
		ALCHEMY_GUIDE.pagesStates.put("Energy_Food",          debugState);
		ALCHEMY_GUIDE.pagesStates.put("Bombs",                debugState);
		//ALCHEMY_GUIDE.pagesStates.put("Darts",              debugState);

		//prison
		ALCHEMY_GUIDE.pagesStates.put("Exotic_Potions",       debugState);
		ALCHEMY_GUIDE.pagesStates.put("Exotic_Scrolls",       debugState);

		//caves
		ALCHEMY_GUIDE.pagesStates.put("Catalysts",            debugState);
		ALCHEMY_GUIDE.pagesStates.put("Brews_Elixirs",        debugState);
		ALCHEMY_GUIDE.pagesStates.put("Spells",               debugState);

		//placeholder lore — content TBD
		LORE_PLACEHOLDER.pagesStates.put("Page1",             debugState);
		LORE_PLACEHOLDER.pagesStates.put("Page2",             debugState);
	}

	private static final String DOCUMENTS = "documents";
	private static final String UNREAD_SUFFIX = "_unread";

	public static void store( Bundle bundle ){

		Bundle docBundle = new Bundle();

		for ( Document doc : values()){
			ArrayList<String> readPages = new ArrayList<>();
			ArrayList<String> unreadPages = new ArrayList<>();
			for (String page : doc.pagesStates.keySet()){
				int state = doc.pagesStates.get(page);
				if (state == READ) {
					readPages.add(page);
				} else if (state == FOUND) {
					unreadPages.add(page);
				}
			}
			if (!readPages.isEmpty()) {
				docBundle.put(doc.name(), readPages.toArray(new String[0]));
			}
			if (!unreadPages.isEmpty()) {
				docBundle.put(doc.name() + UNREAD_SUFFIX, unreadPages.toArray(new String[0]));
			}
		}

		bundle.put( DOCUMENTS, docBundle );

	}

	public static void restore( Bundle bundle ){

		if (!bundle.contains( DOCUMENTS )){
			return;
		}

		Bundle docBundle = bundle.getBundle( DOCUMENTS );

		for ( Document doc : values()){
			// Load FOUND-but-not-READ pages first (new format)
			String unreadKey = doc.name() + UNREAD_SUFFIX;
			if (docBundle.contains(unreadKey)){
				List<String> pages = Arrays.asList(docBundle.getStringArray(unreadKey));
				for (String page : pages){
					if (doc.pagesStates.containsKey(page)) {
						doc.pagesStates.put(page, FOUND);
					}
				}
			}
			// Load READ pages second (also handles old 2-state saves where true → READ)
			if (docBundle.contains(doc.name())){
				List<String> pages = Arrays.asList(docBundle.getStringArray(doc.name()));
				for (String page : pages){
					if (doc.pagesStates.containsKey(page)) {
						doc.pagesStates.put(page, READ);
					}
				}
			}
		}
	}

}
