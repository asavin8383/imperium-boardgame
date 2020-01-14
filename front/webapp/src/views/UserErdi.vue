<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
	<v-layout align-center justify-start column fill-height style="border: solid 0 blue">

		<v-container class="px-2 py-1">
			<v-layout class="mb-3">
				<v-card style="width: 100%">
					<v-layout class="mt-3 mx-3" column>
						<v-flex class="mb-4" raw><h3 class="ml-2">Пользовательские записи типа ЕРДИ</h3></v-flex>
					</v-layout>
					<v-card-title class="pb-0">
						<div style="width: 400px!important;">
							<v-text-field class="ml-2"
										  v-model="search"
										  append-icon="search"
										  label="Поиск"
										  single-line
										  hide-details
							></v-text-field>
						</div>
						<v-spacer></v-spacer>
						<v-btn flat @click="back">
							Отмена
						</v-btn>
						<v-btn color="primary" @click="add" >
							Добавить
						</v-btn>
					</v-card-title>
					<user-erdi-table ref="table" :id.sync="custom_id" :is-have="false" :search.sync="search"></user-erdi-table>

				</v-card>
			</v-layout>

		</v-container>

	</v-layout>


</template>

<script>
    import UserErdiTable from '../components/UserErdiTable'

    export default {
        name: "UserErdi",

        components: {UserErdiTable},

        data() {
            return {
                custom_id: '',
                search:'',
            }
        },

        created() {
            this.custom_id = this.$route.params.id;
        },

        methods: {
            back(){
                this.$router.push({name: 'trafficinfo', params: {id: this.$route.params.traffic_id}});
            },

            add() {
                this.$refs.table.add();
            },

        }
    }
</script>

<style scoped>

</style>