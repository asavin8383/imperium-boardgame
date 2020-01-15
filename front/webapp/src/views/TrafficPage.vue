<template>
	<v-layout align-center justify-start column fill-height style="border: solid 0 blue">
		<v-container class="px-2 py-1">
			<v-layout class="mb-3">
				<v-card style="width: 100%">
					<v-card-title class="pb-4">
						<v-layout align-start justify-space-between row class="mb-2">
							<v-flex md6>
								<v-layout align-center row fill-height class="ml-2 pa-0" >
									<v-flex md3>
										<h3 class="ma-0 pa-0">Наименование</h3>
									</v-flex>
									<v-flex md4>
										<v-text-field v-if="editMode" v-model="data.name" class="ma-0 pa-0"></v-text-field>
										<span v-else class="ma-0 pa-0 subheading">{{data.name}}</span>
									</v-flex>
								</v-layout>
							</v-flex>
							<v-flex md6>
								<v-layout align-end row v-if="editMode">
									<v-spacer md2></v-spacer>
										<v-btn color="error" class="ma-0 right" @click="del">Удалить</v-btn>
										<v-btn flat class="ma-0 right" @click="editMode = !editMode">Отмена</v-btn>
										<v-btn class="ma-0 right" color="primary" @click="saveChanges">Сохранить</v-btn>
								</v-layout>

								<v-btn v-if="!editMode" class=" mr-2 right" color="primary" title="Редактировать" @click="editMode = !editMode">Редактировать</v-btn>
								<v-btn v-if="!editMode" color="error" class="mr-2 right" @click="del">Удалить</v-btn>
							</v-flex>
						</v-layout>
					</v-card-title>
				</v-card>
			</v-layout>
			<v-layout class="mb-3" v-if="draw">
				<v-card style="width: 100%">
					<v-card-title class="pb-0">
						<h3 class="ml-2">Записи ЕРДИ</h3>
						<v-spacer></v-spacer>
						<v-flex md6 class="cube pa-0" v-if="editMode">
							<v-btn icon class=" ma-1 right" color="error" title="Удалить" @click="$refs.erdiTable.delete">
								<v-icon>remove</v-icon>
							</v-btn>
							<v-btn icon class=" ma-1 right" color="primary" title="Добавить" @click="add(1)">
								<v-icon>add</v-icon>
							</v-btn>
						</v-flex>
					</v-card-title>
					<erdi-table ref="erdiTable" :id.sync="erdi_id" :full="false"></erdi-table>
				</v-card>
			</v-layout>
			<v-layout class="mb-3" v-if="draw">
				<v-card style="width: 100%">
					<v-card-title class="pb-0">
						<h3 class="ml-2">Пользовательские записи типа ЕРДИ</h3>
						<v-spacer></v-spacer>
						<v-flex md6 class="cube pa-0" v-if="editMode">
							<v-btn icon class=" ma-1 right" color="error" title="Удалить" @click="$refs.userTable.delete">
								<v-icon>remove</v-icon>
							</v-btn>
							<v-btn icon class=" ma-1 right" color="primary" title="Добавить" @click="add(2)">
								<v-icon>add</v-icon>
							</v-btn>
						</v-flex>
					</v-card-title>
					<user-erdi-table ref="userTable" :id="custom_id" :is-have="true"></user-erdi-table>
				</v-card>
			</v-layout>
<!--			<v-layout class="mb-3" v-if="draw">-->
<!--				<v-card style="width: 100%">-->
<!--					<v-card-title class="pb-0">-->
<!--						<h3 class="ml-2">Словарные выражения</h3>-->
<!--						<v-spacer></v-spacer>-->
<!--						<v-flex md6 class="cube pa-0" v-if="editMode">-->
<!--							<v-btn icon class=" ma-1 right" color="error" title="Удалить" @click="$refs.dicTable.delete">-->
<!--								<v-icon>remove</v-icon>-->
<!--							</v-btn>-->
<!--							<v-btn icon class=" ma-1 right" color="primary" title="Добавить" @click="add(3)">-->
<!--								<v-icon>add</v-icon>-->
<!--							</v-btn>-->
<!--						</v-flex>-->
<!--					</v-card-title>-->
<!--					<dictionary-table ref="dicTable" :id.sync="phrase_id" is-have="true"></dictionary-table>-->
<!--				</v-card>-->
<!--			</v-layout>-->
		</v-container>
	</v-layout>
</template>

<script>
    import ErdiTable from '../components/ErdiTable'
    import UserErdiTable from '../components/UserErdiTable'
    import DictionaryTable from '../components/DictionaryTable'

    // noinspection JSUnusedGlobalSymbols
    export default {
        name: "TrafficPage",

        components: {ErdiTable, UserErdiTable, DictionaryTable},

        data() {
            return {
                id: '',
                editMode: false,
                data: {},
                erdi_id: null,
                custom_id: null,
                phrase_id: null,
				draw: false,

            }
        },

        mounted() {
            this.id = this.$route.params.id;
            this.getTraffic();
        },

        methods: {
            del() {
                let url = `${this.$urls.TRAFFIC}/${this.id}`;
                this.$axios.delete(url).then(() => {
                    this.$router.push({name: 'traffics'});
                }).catch((e) => {
                    console.log('error: ', e);
                });
            },

            add(value) {
                switch (value) {
                    case 1:
                        this.$router.push({name: 'erdilist', params: {traffic_id: this.id, id: this.erdi_id}});
                        break;
                    case 2:
                        this.$router.push({name: 'usererdi', params: {traffic_id: this.id ,id: this.custom_id}});
                        break;
                    case 3:
                        this.$router.push({name: 'dictionary', params: {traffic_id: this.id,id: this.phrase_id}});
                        break;
                }
            },

            getData(data) {
                this.data = data;
                this.erdi_id = data.formalErdiUnit.id;
                this.custom_id = data.customErdiUnit.id;
                this.phrase_id = data.searchTemplateUnit.id;
            },

            getTraffic() {
                let url = `${this.$urls.TRAFFIC}/${this.id}`;
                this.$axios.get(url).then(resp => {
                    this.getData(resp.data);
                }).then(()=>{
                    this.draw = true
                }).catch((e) => {
                    console.log('error: ', e);
                });
            },

            saveChanges() {
                let url = `${this.$urls.TRAFFIC}/${this.id}`;

                this.$axios.put(url, this.data).then(resp => {
                    this.getData(resp.data);
                }).then(() => {
                    this.draw = true
                }).catch((e) => {
                    console.log('error: ', e);
                }).finally(() => {
                    this.editMode = false;
                })
            },

        },

    }
</script>

<style scoped>

</style>