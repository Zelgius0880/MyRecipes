package zelgius.com.myrecipes.adapters

abstract class DataItem(var adapterId: Long) {
    abstract val isSectionHeader: Boolean
    private var nextChildId = 0L

    var isPinned = false

    fun generateNewChildId(): Long {
        val id = nextChildId
        nextChildId += 1
        return id
    }
}

class AdapterDataProvider<P : DataItem, C : DataItem>(val list: MutableList<Pair<P, MutableList<C>>>) {

    // for undo group item
    private var mLastRemovedGroup: Pair<P, MutableList<C>>? = null
    private var mLastRemovedGroupPosition = -1

    // for undo child item
    private var mLastRemovedChild: C? = null
    private var mLastRemovedChildParentGroupId: Long = -1
    private var mLastRemovedChildPosition = -1

    val groupCount: Int
        get() = list.size

    fun getChildCount(groupPosition: Int): Int {
        return list[groupPosition].second.size
    }

    fun getGroupItem(groupPosition: Int): P {
        if (groupPosition < 0 || groupPosition >= groupCount) {
            throw IndexOutOfBoundsException("groupPosition = $groupPosition")
        }

        return list[groupPosition].first
    }

    fun getChildItem(groupPosition: Int, childPosition: Int): C {
        if (groupPosition < 0 || groupPosition >= groupCount) {
            throw ArrayIndexOutOfBoundsException("groupPosition = $groupPosition")
        }

        val children = list[groupPosition].second

        if (childPosition < 0 || childPosition >= children.size) {
            throw IndexOutOfBoundsException("childPosition = $childPosition")
        }

        return children[childPosition]
    }

    fun moveGroupItem(fromGroupPosition: Int, toGroupPosition: Int) {
        if (fromGroupPosition == toGroupPosition) {
            return
        }

        val item = list.removeAt(fromGroupPosition)
        list.add(toGroupPosition, item)
    }

    fun moveChildItem(fromGroupPosition: Int, fromChildPosition: Int, toGroupPosition: Int, toChildPosition: Int) {
        if (fromGroupPosition == toGroupPosition && fromChildPosition == toChildPosition) {
            return
        }

        val (first, second) = list[fromGroupPosition]
        val (first1, second1) = list[toGroupPosition]

        if (first.isSectionHeader) {
            throw IllegalStateException("Source group is a section header!")
        }
        if (first1.isSectionHeader) {
            throw IllegalStateException("Destination group is a section header!")
        }

        val item = second.removeAt(fromChildPosition)

        if (toGroupPosition != fromGroupPosition) {
            // assign a new ID
            val newId = (first1).generateNewChildId()
            item.adapterId = newId
        }

        second1.add(toChildPosition, item)
    }

    fun removeGroupItem(groupPosition: Int) {
        mLastRemovedGroup = list.removeAt(groupPosition)
        mLastRemovedGroupPosition = groupPosition

        mLastRemovedChild = null
        mLastRemovedChildParentGroupId = -1
        mLastRemovedChildPosition = -1
    }

    fun removeChildItem(groupPosition: Int, childPosition: Int) {
        mLastRemovedChild = list[groupPosition].second.removeAt(childPosition)
        mLastRemovedChildParentGroupId = list[groupPosition].first.adapterId
        mLastRemovedChildPosition = childPosition

        mLastRemovedGroup = null
        mLastRemovedGroupPosition = -1
    }

    fun addGroupItem(item: P){
        list.add(item to mutableListOf())
    }


    fun addChildItem(groupPosition: Int,item: C){
        list[groupPosition].second.add(item)
    }
    fun addChildItem(groupPosition: Int, childPosition: Int, item: C){
        list[groupPosition].second.add(childPosition, item)
    }
}