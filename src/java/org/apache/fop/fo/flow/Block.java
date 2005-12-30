/*
 * Copyright 1999-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id: Block.java,v 1.14 2004/04/02 13:50:52 cbowditch Exp $ */

package org.apache.fop.fo.flow;

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.CharIterator;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObjMixed;
import org.apache.fop.fo.NullCharIterator;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAural;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonFont;
import org.apache.fop.fo.properties.CommonHyphenation;
import org.apache.fop.fo.properties.CommonMarginBlock;
import org.apache.fop.fo.properties.CommonRelativePosition;
import org.apache.fop.fo.properties.KeepProperty;
import org.apache.fop.fo.properties.SpaceProperty;

/*
  Modified by Mark Lillywhite mark-fop@inomial.com. The changes
  here are based on memory profiling and do not change functionality.
  Essentially, the Block object had a pointer to a BlockArea object
  that it created. The BlockArea was not referenced after the Block
  was finished except to determine the size of the BlockArea, however
  a reference to the BlockArea was maintained and this caused a lot of
  GC problems, and was a major reason for FOP memory leaks. So,
  the reference to BlockArea was made local, the required information
  is now stored (instead of a reference to the complex BlockArea object)
  and it appears that there are a lot of changes in this file, in fact
  there are only a few sematic changes; mostly I just got rid of
  "this." from blockArea since BlockArea is now local.
 */
 /**
  * Class modelling the fo:block object.
  */
public class Block extends FObjMixed {

    // used for FO validation
    private boolean blockOrInlineItemFound = false;
    private boolean initialPropertySetFound = false;

    // The value of properties relevant for fo:block.
    private CommonAccessibility commonAccessibility;
    private CommonAural commonAural;
    private CommonBorderPaddingBackground commonBorderPaddingBackground;
    private CommonFont commonFont;
    private CommonHyphenation commonHyphenation;
    private CommonMarginBlock commonMarginBlock;
    private CommonRelativePosition commonRelativePosition;
    private int breakAfter;
    private int breakBefore;
    private ColorType color;
    private Length textDepth;
    private Length textAltitude;
    private int hyphenationKeep;
    private Numeric hyphenationLadderCount;
    private String id;
    private int intrusionDisplace;
    private KeepProperty keepTogether;
    private KeepProperty keepWithNext;
    private KeepProperty keepWithPrevious;
    private Length lastLineEndIndent;
    private int linefeedTreatment;
    private SpaceProperty lineHeight;
    private int lineHeightShiftAdjustment;
    private int lineStackingStrategy;
    private Numeric orphans;
    private int whiteSpaceTreatment;
    private int span;
    private int textAlign;
    private int textAlignLast;
    private Length textIndent;
    private int visibility;
    private int whiteSpaceCollapse;
    private Numeric widows;
    private int wrapOption;
    // End of property values
    
    // this may be helpful on other FOs too
    private boolean anythingLaidOut = false;

    /**
     * @param parent FONode that is the parent of this object
     *
     */
    public Block(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#bind(PropertyList)
     */
    public void bind(PropertyList pList) throws FOPException {
        commonAccessibility = pList.getAccessibilityProps();
        commonAural = pList.getAuralProps();
        commonBorderPaddingBackground = pList.getBorderPaddingBackgroundProps();
        commonFont = pList.getFontProps();
        commonHyphenation = pList.getHyphenationProps();
        commonMarginBlock = pList.getMarginBlockProps();
        commonRelativePosition = pList.getRelativePositionProps();

        breakAfter = pList.get(PR_BREAK_AFTER).getEnum();
        breakBefore = pList.get(PR_BREAK_BEFORE).getEnum();
        color = pList.get(PR_COLOR).getColorType();
        textDepth = pList.get(PR_TEXT_DEPTH).getLength();
        textAltitude = pList.get(PR_TEXT_ALTITUDE).getLength();
        hyphenationKeep = pList.get(PR_HYPHENATION_KEEP).getEnum();
        hyphenationLadderCount = pList.get(PR_HYPHENATION_LADDER_COUNT).getNumeric();
        id = pList.get(PR_ID).getString();
        intrusionDisplace = pList.get(PR_INTRUSION_DISPLACE).getEnum();
        keepTogether = pList.get(PR_KEEP_TOGETHER).getKeep();
        keepWithNext = pList.get(PR_KEEP_WITH_NEXT).getKeep();
        keepWithPrevious = pList.get(PR_KEEP_WITH_PREVIOUS).getKeep();
        lastLineEndIndent = pList.get(PR_LAST_LINE_END_INDENT).getLength();
        linefeedTreatment = pList.get(PR_LINEFEED_TREATMENT).getEnum();
        lineHeight = pList.get(PR_LINE_HEIGHT).getSpace();
        lineHeightShiftAdjustment = pList.get(PR_LINE_HEIGHT_SHIFT_ADJUSTMENT).getEnum();
        lineStackingStrategy = pList.get(PR_LINE_STACKING_STRATEGY).getEnum();
        orphans = pList.get(PR_ORPHANS).getNumeric();
        whiteSpaceTreatment = pList.get(PR_WHITE_SPACE_TREATMENT).getEnum();
        span = pList.get(PR_SPAN).getEnum();
        textAlign = pList.get(PR_TEXT_ALIGN).getEnum();
        textAlignLast = pList.get(PR_TEXT_ALIGN_LAST).getEnum();
        textIndent = pList.get(PR_TEXT_INDENT).getLength();
        visibility = pList.get(PR_VISIBILITY).getEnum();
        whiteSpaceCollapse = pList.get(PR_WHITE_SPACE_COLLAPSE).getEnum();
        widows = pList.get(PR_WIDOWS).getNumeric();
        wrapOption = pList.get(PR_WRAP_OPTION).getEnum();
    }

    /**
     * @see org.apache.fop.fo.FONode#startOfNode
     */
    protected void startOfNode() throws FOPException {
        checkId(id);
        getFOEventHandler().startBlock(this);
    }

    /**
     * @see org.apache.fop.fo.FONode#endOfNode
     */
    protected void endOfNode() throws FOPException {
        super.endOfNode();
        getFOEventHandler().endBlock(this);
    }

    /**
     * @return the Common Margin Properties-Block.
     */
    public CommonMarginBlock getCommonMarginBlock() {
        return commonMarginBlock;
    }

    /**
     * @return the Common Border, Padding, and Background Properties.
     */
    public CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
        return commonBorderPaddingBackground;
    }

    /**
     * @return the Common Font Properties.
     */
    public CommonFont getCommonFont() {
        return commonFont;
    }

    /**
     * @return the Common Hyphenation Properties.
     */
    public CommonHyphenation getCommonHyphenation() {
        return commonHyphenation;
    }

    /** @return the "break-after" property. */
    public int getBreakAfter() {
        return breakAfter;
    }

    /** @return the "break-before" property. */
    public int getBreakBefore() {
        return breakBefore;
    }

    /** @return the "hyphenation-ladder-count" property.  */
    public Numeric getHyphenationLadderCount() {
        return hyphenationLadderCount;
    }

    /** @return the "keep-with-next" property.  */
    public KeepProperty getKeepWithNext() {
        return keepWithNext;
    }

    /** @return the "keep-with-previous" property.  */
    public KeepProperty getKeepWithPrevious() {
        return keepWithPrevious;
    }

    /** @return the "keep-together" property.  */
    public KeepProperty getKeepTogether() {
        return keepTogether;
    }

    /** @return the "orphans" property.  */
    public int getOrphans() {
        return orphans.getValue();
    }

    /** @return the "widows" property.  */
    public int getWidows() {
        return widows.getValue();
    }

    /** @return the "line-stacking-strategy" property.  */
    public int getLineStackingStrategy() {
        return lineStackingStrategy;
    }

    /**
     * @return the "color" property.
     */
    public ColorType getColor() {
        return color;
    }

    /**
     * @return the "id" property.
     */
    public String getId() {
        return id;
    }

    /**
     * @return the "line-height" property.
     */
    public SpaceProperty getLineHeight() {
        return lineHeight;
    }

    /**
     * @return the "span" property.
     */
    public int getSpan() {
        return this.span;
    }

    /**
     * @return the "text-align" property.
     */
    public int getTextAlign() {
        return textAlign;
    }

    /**
     * @return the "text-align-last" property.
     */
    public int getTextAlignLast() {
        return textAlignLast;
    }

    /**
     * @return the "text-indent" property.
     */
    public Length getTextIndent() {
        return textIndent;
    }

    /**
     * @return the "last-line-end-indent" property.
     */
    public Length getLastLineEndIndent() {
        return lastLineEndIndent;
    }

    /**
     * @return the "wrap-option" property.
     */
    public int getWrapOption() {
        return wrapOption;
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
     * XSL Content Model: marker* initial-property-set? (#PCDATA|%inline;|%block;)*
     * Additionally: "An fo:bidi-override that is a descendant of an fo:leader
     *  or of the fo:inline child of an fo:footnote may not have block-level
     *  children, unless it has a nearer ancestor that is an 
     *  fo:inline-container."
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
        throws ValidationException {
        if (FO_URI.equals(nsURI) && localName.equals("marker")) {
            if (blockOrInlineItemFound || initialPropertySetFound) {
               nodesOutOfOrderError(loc, "fo:marker", 
                    "initial-property-set? (#PCDATA|%inline;|%block;)");
            }
        } else if (FO_URI.equals(nsURI) && localName.equals("initial-property-set")) {
            if (initialPropertySetFound) {
                tooManyNodesError(loc, "fo:initial-property-set");
            } else if (blockOrInlineItemFound) {
                nodesOutOfOrderError(loc, "fo:initial-property-set", 
                    "(#PCDATA|%inline;|%block;)");
            } else {
                initialPropertySetFound = true;
            }
        } else if (isBlockOrInlineItem(nsURI, localName)) {
            blockOrInlineItemFound = true;
        } else {
            invalidChildError(loc, nsURI, localName);
        }
    }

    /**
     * Accessor for the linefeed-treatment property
     * 
     * @return the enum value of linefeed-treatment
     */
    public int getLinefeedTreatment() {
        return linefeedTreatment;
    }
    
    /**
     * Accessor for the white-space-treatment property
     * 
     * @return the enum value of white-space-treatment
     */
    public int getWhitespaceTreatment() {
        return whiteSpaceTreatment;
    }
    
    /**
     * Accessor for the white-space-collapse property
     * 
     * @return the enum value of white-space-collapse
     */
    public int getWhitespaceCollapse() {
        return whiteSpaceCollapse;
    }
    
    /** @see org.apache.fop.fo.FONode#charIterator() */
    public CharIterator charIterator() {
        return NullCharIterator.getInstance();
    }

    /** @see org.apache.fop.fo.FONode#getLocalName() */
    public String getLocalName() {
        return "block";
    }

    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_BLOCK;
    }
}
