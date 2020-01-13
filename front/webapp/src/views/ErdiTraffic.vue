<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
	<v-layout align-center justify-start column fill-height style="border: solid 0 blue">

		<v-container class="px-2 py-1">
			<v-layout class="mb-3">
				<v-card style="width: 100%">
					<v-layout class="mt-3 mx-3" column>
						<v-flex class="mb-4" raw><h3 class="ml-2">Записи ЕРДИ </h3></v-flex>
					</v-layout>
					<v-card-title class="pb-0">
						<div style="width: 400px!important;">
							<v-text-field class="ml-2"
										  v-model="search"
										  prepend-inner-icon="search"
                                          clearable
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
					<v-layout justify-end row>
						<v-checkbox style="max-width: 325px;"
							v-model="quantityBool"
							label="Добавить определенное кол-во ЕРДИ"
						></v-checkbox>
						<v-text-field
							v-if="quantityBool"
							v-model="quantity"
							style="max-width: 205px;"
							class="number-text-field pr-4"
							label="Введите кол-во ЕРДИ">
						</v-text-field>
					</v-layout>
					<erdi-table ref="table" :id.sync="erdi_id" :full="true" :search.sync="search"></erdi-table>
				</v-card>
			</v-layout>
		</v-container>
	</v-layout>
</template>

<script>
    import ErdiTable from '../components/ErdiTable'

    export default {
        name: "ErdiTraffic",

        components: {ErdiTable},

        data() {
            return {
                erdi_id: '',
                search:'',
				quantityBool: false,
				quantity: null,
            }
        },

        created() {
            this.erdi_id = this.$route.params.id;
        },

        methods: {
            back(){
                this.$router.push({name: 'trafficinfo', params: {id: this.$route.params.traffic_id}});
			},

            add() {
                this.$refs.table.add(this.quantityBool, this.quantity);
            },


        }
    }
</script>

<style scoped>

</style>