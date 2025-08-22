class LabelManager {
    private val labels = mutableMapOf<String, Int>()
    
    fun addLabel(name: String, address: Int) {
        labels[name.trim()] = address
    }
    
    fun getLabel(name: String): Int? {
        return labels[name.trim()]
    }
    
    fun getAllLabels(): Map<String, Int> {
        return labels.toMap()
    }
}
