package ufr.m1.prog_mobile.projet.presentation.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ufr.m1.prog_mobile.projet.data.NotifDelay
import ufr.m1.prog_mobile.projet.data.database.ActiviteBD
import ufr.m1.prog_mobile.projet.data.database.ActivityAnimalBD
import ufr.m1.prog_mobile.projet.data.database.AnimalBD
import ufr.m1.prog_mobile.projet.data.entity.Activite
import ufr.m1.prog_mobile.projet.data.entity.ActiviteAnimal
import ufr.m1.prog_mobile.projet.data.entity.Animal

class AddActViewModel(application: Application) : AndroidViewModel(application) {

    private val animalDao by lazy { AnimalBD.getDB(application).AnimalDao() }
    val currentAnimal = mutableStateOf(Animal(0, "", "", "photo"))

    private val activiteAnimalDao by lazy { ActivityAnimalBD.getDB(application).ActiviteAnimalDao() }
    val listActiviteAnimal = mutableStateOf(listOf<ActiviteAnimal>())

    private val activityDao by lazy { ActiviteBD.getDB(application).ActiviteDao() }
    val listActivity = mutableStateOf(listOf<Activite>())

    fun initializeData(context: Context) {
        // Nettoyer les variables sur le thread principal
        viewModelScope.launch(Dispatchers.Main) {
            currentAnimal.value = Animal(0, "", "", "photo")
            listActiviteAnimal.value = listOf()
            listActivity.value = listOf()

            // Récupérer l'ID depuis l'intent
            val intent = (context as Activity).intent
            val id = intent.getStringExtra("id")!!.toInt()

            // Charger les nouvelles données
            withContext(Dispatchers.IO) {
                val animal = animalDao.getAnimalById(id)
                val activitesAnimal = activiteAnimalDao.getActiviteAnimalById(animalId = id)
                val activities = mutableListOf<Activite>()

                for (activiteAnimal in activitesAnimal) {
                    val activite = activityDao.getActiviteById(activiteAnimal.activityId)
                    activities.add(activite)
                }

                // Mettre à jour les états sur le thread principal
                withContext(Dispatchers.Main) {
                    currentAnimal.value = animal
                    listActiviteAnimal.value = activitesAnimal
                    listActivity.value = activities
                }
            }
        }
    }

    private val _selectedAnimal = MutableStateFlow<Animal?>(null)
    val selectedAnimal: StateFlow<Animal?> = _selectedAnimal

    fun selectAnimal(animal: Animal) {
        viewModelScope.launch {
            _selectedAnimal.value = animal
        }
    }

    private val _selectedDelayType = MutableStateFlow<NotifDelay>(NotifDelay.Unique)
    val selectedDelayType: StateFlow<NotifDelay> = _selectedDelayType

    fun selectDelayType(delayType: NotifDelay) {
        viewModelScope.launch {
            _selectedDelayType.value = delayType
        }
    }

    private val _selectedTime = MutableStateFlow("00:00")
    val selectedTime: StateFlow<String> = _selectedTime

    fun selectTime(time: String) {
        viewModelScope.launch {
            _selectedTime.value = time
        }
    }
}